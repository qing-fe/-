package com.huang.redblack.commen.exception;

/**
 * 业务异常：携带业务状态码，由 {@link GlobalExceptionHandler} 统一转换为 HTTP 响应。
 * <p>
 * HTTP 状态码由业务码推导：扩展码取前三位（401002 -> 401），基础码直接使用（404 -> 404）。
 * 状态码定义见 docs/API-PROTOCOL.md 第 4 章。
 */
public class BusinessException extends RuntimeException {

    /**
     * 业务状态码（200/400/401/403/404/409/429/500 或 401xxx 扩展码）
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * 由业务码推导 HTTP 状态码
     */
    public int getHttpStatus() {
        return code >= 1000 ? code / 1000 : code;
    }
}
