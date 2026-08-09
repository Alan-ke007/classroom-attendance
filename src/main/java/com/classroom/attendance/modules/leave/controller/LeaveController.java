package com.classroom.attendance.modules.leave.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.OperationLog;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.leave.entity.LeaveRequest;
import com.classroom.attendance.modules.leave.service.LeaveRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController extends BaseController {

    private final LeaveRequestService leaveRequestService;

    // 归属裁剪在 service 内按角色执行：学生仅看自己，教师/管理员看全部（H1 修复，避免学生端 403）
    @GetMapping("/list")
    public Result<Page<LeaveRequest>> getLeaveList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String status) {
        return Result.success(leaveRequestService.getLeaveList(pageNum, pageSize, studentId, status));
    }

    @PostMapping("/apply")
    public Result<LeaveRequest> apply(@RequestBody LeaveRequest leaveRequest) {
        if (leaveRequest.getStudentId() == null) {
            leaveRequest.setStudentId(SecurityUtil.getCurrentStudentId());
        }
        return Result.success(leaveRequestService.apply(leaveRequest));
    }

    @RequireRole({"admin", "teacher"})
    @OperationLog(title = "审批通过请假", operation = "update")
    @PutMapping("/approve/{id}")
    public Result<LeaveRequest> approve(@PathVariable Long id, @RequestBody(required = false) LeaveRequest body) {
        Long approverId = body != null ? body.getApproverId() : currentUserIdOrNull();
        String remark = body != null && body.getApproveRemark() != null ? body.getApproveRemark() : "审批通过";
        return Result.success(leaveRequestService.approve(id, approverId != null ? approverId : 1L, remark));
    }

    @RequireRole({"admin", "teacher"})
    @OperationLog(title = "驳回请假", operation = "update")
    @PutMapping("/reject/{id}")
    public Result<LeaveRequest> reject(@PathVariable Long id, @RequestBody(required = false) LeaveRequest body) {
        Long approverId = body != null ? body.getApproverId() : currentUserIdOrNull();
        String remark = body != null && body.getApproveRemark() != null ? body.getApproveRemark() : "不符合请假条件";
        return Result.success(leaveRequestService.reject(id, approverId != null ? approverId : 1L, remark));
    }
}
