package com.huang.redblack.commen.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 统一配置类
 * <p>
 * 对应 application.yml 中的 jwt 节点：
 * jwt:
 *   secret: xxx
 *   access-token-expire: 7200
 *   refresh-token-expire: 604800
 *   refresh-threshold: 600
 */
@Data
@ConfigurationProperties(prefix = "jwt")
public class JWTProperties {

    /**
     * JWT 签名密钥（至少32位，生产环境建议用随机字符串）
     */
    public String secret = "RedBlackDefaultSecretKey2026SpringBootJWT";

    /**
     * Access Token 过期时间（秒），默认 2 小时
     */
    public Long accessTokenExpire = 7200L;

    /**
     * Refresh Token 过期时间（秒），默认 7 天
     */
    public Long refreshTokenExpire = 604800L;

}
