package com.classroom.attendance.modules.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.classroom.attendance.modules.attendance.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("attendance")
public class Attendance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long courseId;
    private Long classId;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String className;

    @TableField(exist = false)
    private String courseName;

    private LocalDate attendanceDate;
    private AttendanceStatus status;
    private LocalDateTime checkInTime;
    private BigDecimal confidence;
    private String imagePath;
    private String remark;

    // 人脸核验（P1）：face_status 复用 confidence(BigDecimal) 作 face_confidence；face_source 标记来源
    private String faceStatus;   // VERIFIED / NEED_REVIEW / REJECTED（F7/F8/F9）
    private String faceSource;   // 来源标记，如 miniapp_checkin

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
