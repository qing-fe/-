package com.huang.redblack.controller;

import com.huang.redblack.commen.result.Result;
import com.huang.redblack.pojo.vo.TokenVO;
import com.huang.redblack.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Token 刷新控制器
 * <p>
 * 职责：处理 Access Token 过期后的刷新请求
 * - 接收前端传来的 Refresh Token（Header: X-Refresh-Token）
 * - 校验 RT 合法性、Redis 有效性
 * - 生成新的 AT + RT（Rotation 策略，旧 RT 作废）
 * - 返回新 Token 给前端，前端更新本地存储并重试原请求
 */
@Slf4j
@RestController
@RequestMapping("/api/qing")
@Tag(name = "Token管理", description = "双Token刷新与吊销")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    /**
     * 刷新 Access Token
     * <p>
     * 前端在检测到 AT 过期（收到 401）时调用此接口
     * 使用 Refresh Token 换取新的 AT + RT
     *
     * @param refreshToken    刷新令牌（请求头 X-Refresh-Token，必填）
     * @param oldAccessToken  旧访问令牌（请求头 Authorization: Bearer xxx，可选；传入则刷新成功后旧 AT 立即作废）
     * @return 新的 TokenVO（accessToken + refreshToken + tokenType + expiresIn）
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用Refresh Token换取新的Access Token和Refresh Token，旧RT作废、旧AT进黑名单")
    public Result<TokenVO> refreshToken(
            @RequestHeader("X-Refresh-Token") String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String oldAccessToken) {

        log.info("收到 Token 刷新请求");
        TokenVO tokenVO = tokenService.refresh(refreshToken, oldAccessToken);
        return Result.success("刷新成功", tokenVO);
    }

    /**
     * 用户主动退出登录
     * <p>
     * 将当前 Access Token 加入黑名单，并删除 Redis 中的 Refresh Token
     * 使用户的所有 Token 立即失效
     *
     * @param accessToken 访问令牌（Header: Authorization Bearer xxx）
     * @param refreshToken 刷新令牌（Header: X-Refresh-Token）
     * @return 退出结果
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登录", description = "吊销当前用户的AT和RT")
    public Result<Void> logout(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("X-Refresh-Token") String refreshToken) {

        // 去除 Bearer 前缀
        String at = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
        tokenService.logout(at, refreshToken);
        log.info("用户已退出登录");
        return Result.success();
    }
}