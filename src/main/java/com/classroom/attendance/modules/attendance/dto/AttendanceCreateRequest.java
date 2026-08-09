package com.classroom.attendance.modules.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AttendanceCreateRequest {
    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    @NotNull(message = "考勤日期不能为空")
    private LocalDate attendanceDate;

    private String status;
    private String remark;
    private String imagePath;
}
