package com.huang.redblack.commen.constant;

/**
 * 异常处理相关常量：业务状态码与提示信息
 * <p>
 * 状态码定义见 docs/API-PROTOCOL.md 第 4 章。
 */
public class ExceptionConstant {

    // ==================== 基础状态码 ====================
    /** 请求参数错误 */
    public static final int BAD_REQUEST = 400;
    /** 未授权 */
    public static final int UNAUTHORIZED = 401;
    /** 禁止访问 */
    public static final int FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int NOT_FOUND = 404;
    /** 请求方法不支持 */
    public static final int METHOD_NOT_ALLOWED = 405;
    /** 资源冲突 */
    public static final int CONFLICT = 409;
    /** 请求过于频繁 */
    public static final int TOO_MANY_REQUESTS = 429;
    /** 服务端错误 */
    public static final int INTERNAL_ERROR = 500;

    // ==================== 认证扩展码 ====================
    /** Access Token 过期 */
    public static final int ACCESS_TOKEN_EXPIRED = 401001;
    /** Refresh Token 过期 */
    public static final int REFRESH_TOKEN_EXPIRED = 401002;
    /** Token 类型错误 */
    public static final int TOKEN_TYPE_ERROR = 401003;
    /** 账号被锁定 */
    public static final int ACCOUNT_LOCKED = 401004;
    /** 登录状态异常（RT 重放攻击，全部 Token 被吊销） */
    public static final int LOGIN_STATUS_ERROR = 401005;
    /** 设备指纹不匹配 */
    public static final int DEVICE_MISMATCH = 401006;

    // ==================== 提示信息 ====================
    /** 请求参数错误 */
    public static final String MSG_BAD_REQUEST = "请求参数错误";
    /** 请求方法不支持 */
    public static final String MSG_METHOD_NOT_ALLOWED = "请求方法不支持";
    /** 资源不存在 */
    public static final String MSG_NOT_FOUND = "资源不存在";
    /** 服务器内部错误 */
    public static final String MSG_INTERNAL_ERROR = "服务器内部错误";
    /** 用户不存在 */
    public static final String MSG_USER_NOT_FOUND = "用户不存在";
    /** 缺少访问令牌 */
    public static final String MSG_TOKEN_MISSING = "缺少访问令牌，请先登录";
    /** 令牌已失效（黑名单命中） */
    public static final String MSG_TOKEN_INVALID = "令牌已失效，请重新登录";
    /** 非法的访问令牌 */
    public static final String MSG_TOKEN_ILLEGAL = "非法的访问令牌";
    /** 访问令牌已过期 */
    public static final String MSG_TOKEN_EXPIRED = "访问令牌已过期";
    /** 登录状态已过期（Refresh Token 过期） */
    public static final String MSG_LOGIN_EXPIRED = "登录状态已过期，请重新登录";
    /** 非法的登录凭证 */
    public static final String MSG_INVALID_CREDENTIAL = "非法的登录凭证";
    /** 非法的刷新凭证 */
    public static final String MSG_INVALID_REFRESH_CREDENTIAL = "非法的刷新凭证";
    /** 登录状态已失效 */
    public static final String MSG_LOGIN_INVALID = "登录状态已失效，请重新登录";
    /** 登录状态异常（重放攻击） */
    public static final String MSG_LOGIN_ABNORMAL = "登录状态异常，请重新登录";

    private ExceptionConstant() {
    }
}
