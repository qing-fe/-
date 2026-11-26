package com.huang.redblack.commen.utils;

import com.huang.redblack.commen.properties.JWTProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTUtil {
    @Autowired
    private JWTProperties jwtProperties;

    /**
     * 密钥（至少 256 位，建议从配置文件读取）
     * 生产环境不要硬编码！
     */
    private static  String SECRET;

    /**
     * 过期时间：7 天（单位：毫秒）
     */
    private static  long EXPIRATION;

    /**
     * 生成安全的密钥对象
     */
    private static  SecretKey KEY;

    @PostConstruct
    public void init() {
        SECRET = jwtProperties.getSecret();
        EXPIRATION = jwtProperties.getExpiration();
        KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }
    /**
     * 生成 Token
     * @param userId 用户ID
     * @param email 用户邮箱（可选，存入 claims）
     * @return JWT 字符串
     */
    public static String generateToken(Long userId, String email) {
        System.out.println("生成 Token，密钥：" + SECRET + "，过期时间：" + EXPIRATION);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .claims(claims)                     // 自定义数据
                .subject(String.valueOf(userId))    // 主题（用户ID）
                .issuedAt(now)                      // 签发时间
                .expiration(expiration)             // 过期时间
                .signWith(KEY)                      // 签名
                .compact();
    }

    /**
     * 解析 Token，获取 Claims（全部数据）
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 验证 Token 是否有效
     * @return true-有效  false-过期或非法
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token 已过期");
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            System.out.println("Token 非法: " + e.getMessage());
            return false;
        }
    }

    /**
     * 判断 Token 是否即将过期（用于刷新）
     * @param remainTime 剩余时间（毫秒），例如 30 分钟
     */
    public static boolean isTokenExpiredSoon(String token, long remainTime) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        return (expiration.getTime() - System.currentTimeMillis()) < remainTime;
    }
}