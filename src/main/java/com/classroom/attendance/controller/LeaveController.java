package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.model.LeaveRequest;
import com.classroom.attendance.service.LeaveRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    /**
     * 分页查询请假列表
     */
    @GetMapping("/list")
    public Result<Page<LeaveRequest>> getLeaveList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status) {
        log.info("查询请假列表，pageNum: {}, pageSize: {}, studentId: {}, status: {}", pageNum, pageSize, studentId, status);
        try {
            Page<LeaveRequest> page = leaveRequestService.getLeaveList(pageNum, pageSize, studentId, status);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询请假列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 提交请假申请
     */
    @PostMapping("/apply")
    public Result<LeaveRequest> apply(@RequestBody LeaveRequest leaveRequest) {
        log.info("接收到请假申请，学生ID: {}", leaveRequest.getStudentId());
        try {
            LeaveRequest result = leaveRequestService.apply(leaveRequest);
            return Result.success(result);
        } catch (Exception e) {
            log.error("提交请假申请失败", e);
            return Result.error("申请失败: " + e.getMessage());
        }
    }

    /**
     * 审批通过
     */
    @RequireRole({"admin", "teacher"})
    @PutMapping("/approve/{id}")
    public Result<LeaveRequest> approve(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveRequest body) {
        log.info("审批通过请假申请，ID: {}", id);
        try {
            Long approverId = body != null ? body.getApproverId() : 1L;
            String remark = body != null && body.getApproveRemark() != null ? body.getApproveRemark() : "审批通过";
            LeaveRequest result = leaveRequestService.approve(id, approverId, remark);
            return Result.success(result);
        } catch (Exception e) {
            log.error("审批失败", e);
            return Result.error("审批失败: " + e.getMessage());
        }
    }

    /**
     * 驳回申请
     */
    @RequireRole({"admin", "teacher"})
    @PutMapping("/reject/{id}")
    public Result<LeaveRequest> reject(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveRequest body) {
        log.info("驳回请假申请，ID: {}", id);
        try {
            Long approverId = body != null ? body.getApproverId() : 1L;
            String remark = body != null && body.getApproveRemark() != null ? body.getApproveRemark() : "不符合请假条件";
            LeaveRequest result = leaveRequestService.reject(id, approverId, remark);
            return Result.success(result);
        } catch (Exception e) {
            log.error("驳回失败", e);
            return Result.error("驳回失败: " + e.getMessage());
        }
    }
}
