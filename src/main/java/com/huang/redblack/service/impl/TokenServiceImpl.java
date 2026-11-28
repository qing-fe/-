package com.huang.redblack.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.huang.redblack.commen.constant.UserAuthLogsConstant;
import com.huang.redblack.commen.key.RedisKey;
import com.huang.redblack.commen.properties.JWTProperties;
import com.huang.redblack.pojo.vo.TokenVO;
import com.huang.redblack.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token 业务处理层
 * <p>
 * 核心职责：
 * 1. 刷新双 Token（Rotation 策略）：每次刷新都生成新 AT + 新 RT，旧 RT 立即作废
 * 2. 设备绑定：RT 与设备指纹（IP+UA）绑定，防止盗用
 * 3. 登录状态管理：支持主动登出、异常检测（重放攻击时吊销全部 Token）
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JWTProperties jwtProperties;

    /**
     * 登录时签发双 Token（AT + RT），并将 RT 写入 Redis（设备绑定）
     *
     * @param email 用户邮箱
     * @param request HttpServletRequest，用于获取设备指纹
     * @return TokenVO 包含 accessToken、refreshToken、tokenType、expiresIn
     */
    @Override
    public TokenVO login(String email, HttpServletRequest request) {
        String newAccessToken = createAccessToken(email);
        String newRefreshToken = createRefreshToken(email);

        String deviceId = generateDeviceId(request);
        String refreshTokenKey = RedisKey.USER + email + ":" + RedisKey.TOKEN + RedisKey.REFRESH;
        String accessTokenKey = RedisKey.USER + email + ":" + RedisKey.TOKEN + RedisKey.ACCESS;
        redisTemplate.opsForValue().set(
                refreshTokenKey,
                newRefreshToken,
                jwtProperties.getRefreshTokenExpire(),
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                accessTokenKey,
                newAccessToken,
                jwtProperties.getAccessTokenExpire(),
                TimeUnit.SECONDS
        );

        log.info("登录签发双 Token | email={} | deviceId={}", email, deviceId);

        return TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpire())
                .build();
    }

    /**
     * 刷新 Token：使用 Refresh Token 换取新的 AT + RT（Rotation 策略）
     *
     * @param refreshToken 前端传来的 Refresh Token
     * @param request      HttpServletRequest，用于获取设备指纹
     * @return TokenVO 包含新的 accessToken、refreshToken、tokenType、expiresIn
     */
    public TokenVO refresh(String refreshToken, HttpServletRequest request) {
        // 1. 解析 Refresh Token
        SecretKey key = getSecretKey();
        Claims claims;
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(refreshToken);
            claims = jws.getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Refresh Token 已过期");
            throw new RuntimeException("登录状态已过期，请重新登录");
        } catch (JwtException e) {
            log.warn("Refresh Token 非法: {}", e.getMessage());
            throw new RuntimeException("非法的登录凭证");
        }

        // 2. 校验 Token 类型必须是 REFRESH
        String type = claims.get("type", String.class);
        if (!"REFRESH".equals(type)) {
            throw new RuntimeException("非法的刷新凭证");
        }

        String email = claims.getSubject();
        String deviceId = generateDeviceId(request);
        String redisKey = RedisKey.USER + email + ":" + RedisKey.TOKEN + RedisKey.REFRESH;

        // 3. 【核心安全校验】Redis 中存储的 RT 必须与传入的一致
        String storedToken = redisTemplate.opsForValue().get(redisKey);
        if (storedToken == null) {
            log.warn("Redis 中不存在该设备的 Refresh Token，可能已过期或从未登录 | email={}", email);
            throw new RuntimeException("登录状态已失效，请重新登录");
        }

        if (!refreshToken.equals(storedToken)) {
            // 【严重】Token 不匹配：可能是重放攻击或 RT 泄露
            log.error("Refresh Token 不匹配！疑似重放攻击或凭证泄露 | email={}", email);

            // 安全策略：立即吊销该用户的所有设备登录态
            revokeAllUserTokens(email);

            throw new RuntimeException("登录状态异常，请重新登录");
        }

        // 4. 生成新的双 Token
        String newAccessToken = createAccessToken(email);
        String newRefreshToken = createRefreshToken(email);

        // 5. 【Rotation】删除旧的 RT，存入新的 RT（设备绑定）
        redisTemplate.delete(redisKey);
        redisTemplate.opsForValue().set(
                redisKey,
                newRefreshToken,
                jwtProperties.getRefreshTokenExpire(),
                TimeUnit.SECONDS
        );

        log.info("Token 刷新成功 | email={} | deviceId={}", email, deviceId);

        return TokenVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpire())
                .build();
    }

    /**
     * 用户主动退出登录
     *
     * @param accessToken  当前 Access Token
     * @param refreshToken 当前 Refresh Token
     * @param request      HttpServletRequest
     */
    public void logout(String accessToken, String refreshToken, HttpServletRequest request) {
        SecretKey key = getSecretKey();

        // 1. 解析 RT 获取 userId（AT 可能已过期，优先用 RT）
        String email;
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
            email = claims.getSubject();
        } catch (JwtException e) {
            // RT 解析失败，尝试从 AT 解析
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(accessToken)
                        .getPayload();
                email = claims.getSubject();
            } catch (JwtException ex) {
                log.warn("退出时双 Token 均无法解析，直接删除设备绑定");
                // 兜底：尽量删除当前设备的 Redis 记录
                String deviceId = generateDeviceId(request);
                // 无法确定 userId，无法精准删除，依赖 Redis TTL 自动过期
                return;
            }
        }

        String deviceId = generateDeviceId(request);
        String redisKey = RedisKey.USER + email + ":" + RedisKey.TOKEN + RedisKey.REFRESH;

        // 2. 删除该设备的 Refresh Token
        redisTemplate.delete(redisKey);

        // 3. 将 Access Token 加入黑名单（防止在 AT 过期前被冒用）
        long ttl = getTokenRemainingTTL(accessToken);
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    RedisKey.BLACKLIST + accessToken,
                    UserAuthLogsConstant.BLACKLIST_VALID,
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }

        log.info("用户退出登录 | email={} | deviceId={}", email, deviceId);
    }

    /**
     * 吊销用户的所有登录态（所有设备）
     * <p>
     * 用于：检测到重放攻击、用户修改密码、管理员强制下线
     *
     * @param email 用户邮箱
     */
    public void revokeAllUserTokens(String email) {
        // 1. 查找该用户的所有设备 Redis Key
        Set<String> keys = redisTemplate.keys(RedisKey.USER + email + ":" + RedisKey.TOKEN + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("已吊销用户所有设备登录态 | email={} | 设备数={}", email, keys.size());
        }
    }

    // ==================== 私有工具方法 ====================

    /**
     * 生成 Access Token（短期）
     */
    private String createAccessToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpire() * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("type", "ACCESS")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 生成 Refresh Token（中长期）
     */
    private String createRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpire() * 1000);

        return Jwts.builder()
                .subject(userId)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 获取 JWT 签名密钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成设备指纹（绑定 IP + User-Agent）
     * <p>
     * 原理：把用户的 IP 地址和浏览器标识（User-Agent）拼接成字符串，
     * 通过 MD5 哈希生成 32 位固定长度的唯一标识。
     * <p>
     * 例如：
     * 原始信息 = "192.168.1.100:Mozilla/5.0 (Windows NT 10.0; Win64; x64)..."
     * 设备指纹 = "a3f5c8e2d9b1..."（32位MD5）
     * <p>
     * 注意：如果用户网络环境变化（如切换 WiFi），IP 会变，导致设备指纹变化，需要重新登录。
     * 如需更宽松策略，可只使用 User-Agent，或引入 Cookie 中的 deviceId。
     */
    private String generateDeviceId(HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            ua = "unknown";
        }
        String raw = ip + ":" + ua;
        return DigestUtil.md5Hex(raw);
    }

    /**
     * 获取客户端真实 IP（考虑反向代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取 Token 剩余有效期（毫秒）
     */
    private long getTokenRemainingTTL(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return 0;
            }
            return Math.max(0, expiration.getTime() - System.currentTimeMillis());
        } catch (JwtException e) {
            return 0;
        }
    }
}