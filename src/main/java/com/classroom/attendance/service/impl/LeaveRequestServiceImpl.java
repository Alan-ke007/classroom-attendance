package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.classroom.attendance.mapper.LeaveRequestMapper;
import com.classroom.attendance.model.LeaveRequest;
import com.classroom.attendance.service.LeaveRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class LeaveRequestServiceImpl extends ServiceImpl<LeaveRequestMapper, LeaveRequest> implements LeaveRequestService {

    @Override
    public Page<LeaveRequest> getLeaveList(int pageNum, int pageSize, Long studentId, String status) {
        LambdaQueryWrapper<LeaveRequest> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(LeaveRequest::getStudentId, studentId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LeaveRequest::getStatus, status);
        }
        wrapper.orderByDesc(LeaveRequest::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public LeaveRequest apply(LeaveRequest leaveRequest) {
        leaveRequest.setStatus("pending");
        save(leaveRequest);
        log.info("学生 {} 提交请假申请，ID: {}", leaveRequest.getStudentId(), leaveRequest.getId());
        return leaveRequest;
    }

    @Override
    public LeaveRequest approve(Long id, Long approverId, String remark) {
        LeaveRequest lr = getById(id);
        if (lr == null) {
            throw new RuntimeException("请假申请不存在");
        }
        if (!"pending".equals(lr.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }
        lr.setStatus("approved");
        lr.setApproverId(approverId);
        lr.setApproveRemark(remark);
        lr.setApproveTime(LocalDateTime.now());
        updateById(lr);
        log.info("审批通过请假申请，ID: {}", id);
        return lr;
    }

    @Override
    public LeaveRequest reject(Long id, Long approverId, String remark) {
        LeaveRequest lr = getById(id);
        if (lr == null) {
            throw new RuntimeException("请假申请不存在");
        }
        if (!"pending".equals(lr.getStatus())) {
            throw new RuntimeException("该申请已处理");
        }
        lr.setStatus("rejected");
        lr.setApproverId(approverId);
        lr.setApproveRemark(remark);
        lr.setApproveTime(LocalDateTime.now());
        updateById(lr);
        log.info("驳回请假申请，ID: {}", id);
        return lr;
    }
}
