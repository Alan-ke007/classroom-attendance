package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.classroom.attendance.model.LeaveRequest;

public interface LeaveRequestService extends IService<LeaveRequest> {

    Page<LeaveRequest> getLeaveList(int pageNum, int pageSize, Long studentId, String status);

    LeaveRequest apply(LeaveRequest leaveRequest);

    LeaveRequest approve(Long id, Long approverId, String remark);

    LeaveRequest reject(Long id, Long approverId, String remark);
}
