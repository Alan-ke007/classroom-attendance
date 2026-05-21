package com.classroom.attendance.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("attendance")
public class Attendance implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID
     */
    private Long studentId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 班级ID
     */
    private Long classId;
    
    /**
     * 学生姓名（非数据库字段）
     */
    @TableField(exist = false)
    private String studentName;
    
    /**
     * 班级名称（非数据库字段）
     */
    @TableField(exist = false)
    private String className;
    
    /**
     * 课程名称（非数据库字段）
     */
    @TableField(exist = false)
    private String courseName;
    
    /**
     * 考勤日期
     */
    private LocalDate attendanceDate;
    
    /**
     * 状态（present/absent/late/leave）
     */
    private String status;
    
    /**
     * 签到时间
     */
    private LocalDateTime checkInTime;
    
    /**
     * 识别置信度
     */
    private BigDecimal confidence;
    
    /**
     * 抓拍图片路径
     */
    private String imagePath;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标识（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}
