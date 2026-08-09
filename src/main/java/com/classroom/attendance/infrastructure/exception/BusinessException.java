package com.classroom.attendance.infrastructure.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) throw new BusinessException(message);
    }

    public static void notNull(Integer code, Object obj, String message) {
        if (obj == null) throw new BusinessException(code, message);
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) throw new BusinessException(message);
    }

    public static void isTrue(Integer code, boolean condition, String message) {
        if (!condition) throw new BusinessException(code, message);
    }
}
