package com.classroom.attendance.infrastructure.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return Result.<T>builder().code(200).message("操作成功").build();
    }

    public static <T> Result<T> success(T data) {
        return Result.<T>builder().code(200).message("操作成功").data(data).build();
    }

    public static <T> Result<T> success(String message, T data) {
        return Result.<T>builder().code(200).message(message).data(data).build();
    }

    public static <T> Result<T> fail(String message) {
        return Result.<T>builder().code(400).message(message).build();
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return Result.<T>builder().code(code).message(message).build();
    }

    public static <T> Result<T> unauthorized(String message) {
        return Result.<T>builder().code(401).message(message).build();
    }

    public static <T> Result<T> forbidden(String message) {
        return Result.<T>builder().code(403).message(message).build();
    }

    public static <T> Result<T> notFound(String message) {
        return Result.<T>builder().code(404).message(message).build();
    }

    public static <T> Result<T> error(String message) {
        return Result.<T>builder().code(500).message(message).build();
    }
}
