package com.classroom.attendance.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private Integer code;      // 状态码：200成功，其他失败
    private String message;    // 响应消息
    private T data;           // 响应数据
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .build();
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("操作成功")
                .data(data)
                .build();
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 失败响应
     */
    public static <T> Result<T> fail(String message) {
        return Result.<T>builder()
                .code(400)
                .message(message)
                .build();
    }
    
    /**
     * 失败响应（自定义状态码）
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }
    
    /**
     * 未授权响应
     */
    public static <T> Result<T> unauthorized(String message) {
        return Result.<T>builder()
                .code(401)
                .message(message)
                .build();
    }
    
    /**
     * 服务器错误响应
     */
    public static <T> Result<T> error(String message) {
        return Result.<T>builder()
                .code(500)
                .message(message)
                .build();
    }
}
