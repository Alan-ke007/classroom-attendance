package com.classroom.attendance.infrastructure.exception;

import lombok.Getter;

/**
 * 算法服务不可达（401/503/连接超时/网络错）。
 * 业务码固定 40005（ALGO_UNAVAILABLE），由 GlobalExceptionHandler 映射为 Result{code:40005}。
 */
@Getter
public class AlgoUnavailableException extends BusinessException {

    public AlgoUnavailableException(String message) {
        super(40005, message);
    }

    public AlgoUnavailableException(String message, Throwable cause) {
        super(40005, message, cause);
    }
}
