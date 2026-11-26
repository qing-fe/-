package com.huang.redblack.commen.result;

import java.time.LocalDateTime;

public class Result<T> {

    private int code;
    private String massage;
    private T data;
    private LocalDateTime timestamp;

    private Result(){
        this.timestamp = LocalDateTime.now();
    }

    public Result(String massage, int code, T data) {
        this.massage = massage;
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

    public static <T> Result<T> success(String massage, T data){
        return new Result<>(massage, 200, data);
    }

    // ==================== 失败 ====================
    public static <T> Result<T> error(String massage){
        return new Result<>(massage, 500, null);
    }

    public static <T> Result<T> error(String massage, T data){
        return new Result<>(massage, 500, data);
    }
}
