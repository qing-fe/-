package com.huang.redblack.commen.exception;

import com.huang.redblack.commen.constant.ExceptionConstant;
import com.huang.redblack.commen.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理器
 * <p>
 * 统一把各类异常转换为 {@link Result} JSON 结构：
 * <ul>
 *   <li>业务异常（{@link BusinessException}）：按业务码映射 HTTP 状态码；</li>
 *   <li>请求参数错误（JSON 解析失败、缺少请求头/参数等）：统一 400；</li>
 *   <li>请求方法不支持：405；静态资源/路径不存在：404；</li>
 *   <li>其它未预期异常：统一 500，不向客户端泄露堆栈信息。</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：按业务码推导 HTTP 状态码，返回统一 Result
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常 | code={} | message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .body(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 请求参数错误：JSON 解析失败、缺少请求头、缺少参数、参数类型不匹配
     */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Result<Void>> handleBadRequest(Exception e) {
        log.warn("请求参数错误: {}", e.getMessage());
        return ResponseEntity.status(ExceptionConstant.BAD_REQUEST)
                .body(Result.error(ExceptionConstant.BAD_REQUEST, ExceptionConstant.MSG_BAD_REQUEST));
    }

    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {} {}", e.getMethod(), e.getMessage());
        return ResponseEntity.status(ExceptionConstant.METHOD_NOT_ALLOWED)
                .body(Result.error(ExceptionConstant.METHOD_NOT_ALLOWED, ExceptionConstant.MSG_METHOD_NOT_ALLOWED));
    }

    /**
     * 资源/路径不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNotFound(NoResourceFoundException e) {
        log.warn("资源不存在: {}", e.getResourcePath());
        return ResponseEntity.status(ExceptionConstant.NOT_FOUND)
                .body(Result.error(ExceptionConstant.NOT_FOUND, ExceptionConstant.MSG_NOT_FOUND));
    }

    /**
     * 未预期的运行时异常：不向客户端泄露内部信息
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Void>> handleRuntimeException(RuntimeException e) {
        log.error("未预期的运行时异常", e);
        return ResponseEntity.status(ExceptionConstant.INTERNAL_ERROR)
                .body(Result.error(ExceptionConstant.INTERNAL_ERROR, ExceptionConstant.MSG_INTERNAL_ERROR));
    }

    /**
     * 兜底：其它所有异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("未知异常", e);
        return ResponseEntity.status(ExceptionConstant.INTERNAL_ERROR)
                .body(Result.error(ExceptionConstant.INTERNAL_ERROR, ExceptionConstant.MSG_INTERNAL_ERROR));
    }
}
