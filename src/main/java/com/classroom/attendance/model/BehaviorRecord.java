package com.classroom.attendance.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行为识别记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("behavior")
public class BehaviorRecord implements Serializable {
    
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
     * 班级ID
     */
    private Long classId;
    
    /**
     * 课程ID
     */
    private Long courseId;
    
    /**
     * 行为类型（sleeping/phone/eating/etc.）
     */
    private String behaviorType;
    
    /**
     * 行为发生时间
     */
    private LocalDateTime behaviorTime;
    
    /**
     * 识别置信度
     */
    private BigDecimal confidence;
    
    /**
     * 抓拍图片路径
     */
    private String imagePath;
    
    /**
     * 是否已处理（0-未处理，1-已处理）
     */
    private Integer handled;
    
    /**
     * 处理备注
     */
    private String handleRemark;
    
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
}
