package com.huang.redblack.commen.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JWTProperties {
    private String secret; // JWT secret key
    private int expiration; // JWT expiration time in seconds
    private String adminTokenName; // JWT token name for admin
}
