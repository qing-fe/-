package com.huang.redblack.commen.result;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    private Result(){
        this.timestamp = LocalDateTime.now();
    }

    public Result(String message, int code, T data) {
        this.message = message;
        this.code = code;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ==================== 成功 ====================

    public static <T> Result<T> success(){
        return new Result<>("success", 200, null);
    }

    public static <T> Result<T> success(T data){
        return new Result<>("success", 200, data);
    }

    public static <T> Result<T> success(String message, T data){
        return new Result<>(message, 200, data);
    }

    // ==================== 失败 ====================
    public static <T> Result<T> error(String message){
        return new Result<>(message, 500, null);
    }

    public static <T> Result<T> error(String message, T data){
        return new Result<>(message, 500, data);
    }
}
