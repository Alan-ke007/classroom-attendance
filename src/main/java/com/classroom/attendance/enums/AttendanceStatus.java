package com.classroom.attendance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考勤状态枚举
 */
@Getter
@AllArgsConstructor
public enum AttendanceStatus {
    
    /**
     * 出勤
     */
    PRESENT(1, "出勤"),
    
    /**
     * 迟到
     */
    LATE(2, "迟到"),
    
    /**
     * 早退
     */
    EARLY_LEAVE(3, "早退"),
    
    /**
     * 缺勤
     */
    ABSENT(4, "缺勤"),
    
    /**
     * 请假
     */
    LEAVE(5, "请假");
    
    private final Integer code;
    private final String description;
    
    public static AttendanceStatus fromCode(Integer code) {
        for (AttendanceStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的考勤状态码: " + code);
    }
}
