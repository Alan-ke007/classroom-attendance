package com.classroom.attendance.modules.attendance.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AttendanceStatus {

    PRESENT(1, "present", "出勤"),
    LATE(2, "late", "迟到"),
    EARLY_LEAVE(3, "early_leave", "早退"),
    ABSENT(4, "absent", "缺勤"),
    LEAVE(5, "leave", "请假");

    private final Integer code;

    @EnumValue
    private final String dbValue;

    @JsonValue
    private final String description;

    public static AttendanceStatus fromCode(Integer code) {
        for (AttendanceStatus s : values()) {
            if (s.getCode().equals(code)) return s;
        }
        throw new IllegalArgumentException("无效的考勤状态码: " + code);
    }

    public static AttendanceStatus fromDbValue(String dbValue) {
        if (dbValue == null) return null;
        for (AttendanceStatus s : values()) {
            if (s.getDbValue().equals(dbValue)) return s;
        }
        throw new IllegalArgumentException("无效的考勤状态: " + dbValue);
    }
}
