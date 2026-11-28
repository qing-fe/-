package com.huang.redblack.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token 刷新响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVO {

    /**
     * 访问令牌（短期，默认2小时）
     */
    private String accessToken;

    /**
     * 刷新令牌（中长期，默认7天）
     * 启用 Rotation 时，每次刷新都会生成新的 Refresh Token
     */
    private String refreshToken;

    /**
     * Token 类型，固定 Bearer
     */
    private String tokenType;

    /**
     * Access Token 过期时间（秒）
     */
    private Long expiresIn;
}