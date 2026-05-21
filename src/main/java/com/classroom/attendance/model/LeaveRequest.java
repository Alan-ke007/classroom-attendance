package com.classroom.attendance.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("leave_request")
public class LeaveRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long courseId;

    private Long classId;

    /** 请假类型：sick(病假), personal(事假), other(其他) */
    private String leaveType;

    /** 请假开始日期 */
    private LocalDate startDate;

    /** 请假结束日期 */
    private LocalDate endDate;

    /** 请假事由 */
    private String reason;

    /** 证明图片路径 */
    private String proofImage;

    /** 状态：pending(待审批), approved(已通过), rejected(已驳回) */
    private String status;

    /** 审批人ID */
    private Long approverId;

    /** 审批意见 */
    private String approveRemark;

    /** 审批时间 */
    private LocalDateTime approveTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
