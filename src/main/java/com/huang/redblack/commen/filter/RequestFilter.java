package com.huang.redblack.commen.filter;

import tools.jackson.databind.ObjectMapper;
import com.huang.redblack.commen.properties.JWTProperties;
import com.huang.redblack.commen.result.Result;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;

/**
 * JWT Token 认证过滤器
 * <p>
 * 职责：校验 Access Token 是否有效
 * - 公开接口直接放行
 * - AT 无效/过期 → 返回 401，由前端拦截器自动调用 /api/auth/refresh 换取新 AT
 * - 不涉及刷新逻辑，刷新在 TokenController 中独立处理
 */
@Slf4j
@Component
@Order(1)
public class RequestFilter extends OncePerRequestFilter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JWTProperties jwtProperties;

    /**
     * 公开路径：不需要携带 AT 即可访问
     */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/qing/login",
            "/api/qing/refresh",
            "/api/qing/captcha",
            "/api/qing/register",
            "/swagger-ui",
            "/v3/api-docs"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 1. 公开接口直接放行
        if (isPublicPath(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 提取 Access Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "缺少访问令牌，请先登录");
            return;
        }

        String accessToken = authHeader.substring(7);

        // 3. 校验 Access Token
        try {
            // 3.1 检查是否在黑名单（用户主动登出）
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey("blacklist:at:" + accessToken))) {
                writeUnauthorized(response, "令牌已失效，请重新登录");
                return;
            }

            // 3.2 解析并校验过期时间
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken);

            Claims claims = jws.getPayload();

            // 3.3 校验 Token 类型必须是 ACCESS
            String type = claims.get("type", String.class);
            if (!"ACCESS".equals(type)) {
                writeUnauthorized(response, "非法的访问令牌");
                return;
            }

            // 3.4 校验过期时间（双重保险）
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                writeUnauthorized(response, "访问令牌已过期");
                return;
            }

            // 3.5 将用户信息暂存到 request 属性，供下游 Controller 使用
            request.setAttribute("userId", claims.getSubject());
            request.setAttribute("username", claims.get("username", String.class));

        } catch (ExpiredJwtException e) {
            log.warn("AT 已过期: {}", e.getMessage());
            writeUnauthorized(response, "访问令牌已过期");
            return;
        } catch (JwtException e) {
            log.warn("AT 解析失败: {}", e.getMessage());
            writeUnauthorized(response, "非法的访问令牌");
            return;
        }

        // 4. 放行到 Controller
        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否为公开路径（支持前缀匹配）
     */
    private boolean isPublicPath(String uri) {
        return PUBLIC_PATHS.stream().anyMatch(uri::startsWith);
    }

    /**
     * 返回 401 统一响应
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        // Result 类暂无自定义 code 的构造方法，直接构造 JSON
        String json = String.format(
                "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":\"%s\"}",
                message,
                java.time.LocalDateTime.now().toString()
        );
        response.getWriter().write(json);
    }
}