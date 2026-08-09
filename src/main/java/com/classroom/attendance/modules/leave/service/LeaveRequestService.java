package com.classroom.attendance.modules.leave.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.constant.Constants;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.enums.AttendanceStatus;
import com.classroom.attendance.modules.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.leave.entity.LeaveRequest;
import com.classroom.attendance.modules.leave.mapper.LeaveRequestMapper;
import com.classroom.attendance.modules.notification.entity.Notification;
import com.classroom.attendance.modules.notification.mapper.NotificationMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveRequestService {

    private final LeaveRequestMapper leaveRequestMapper;
    private final AttendanceMapper attendanceMapper;
    private final NotificationMapper notificationMapper;
    private final CourseMapper courseMapper;
    private final StudentMapper studentMapper;

    public Page<LeaveRequest> getLeaveList(int pageNum, int pageSize, Long studentId, String status) {
        Page<LeaveRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LeaveRequest> w = new LambdaQueryWrapper<>();
        // 归属裁剪：学生强制只看自己；教师/管理员可指定 studentId 或查全部
        Long resolvedStudentId = studentId;
        if (Constants.Role.STUDENT.equals(SecurityUtil.getCurrentRole())) {
            Long current = SecurityUtil.getCurrentStudentId();
            BusinessException.notNull(current, "未获取到学生身份，请重新登录");
            resolvedStudentId = current;
        }
        if (resolvedStudentId != null) w.eq(LeaveRequest::getStudentId, resolvedStudentId);
        if (status != null && !status.isEmpty()) w.eq(LeaveRequest::getStatus, status);
        w.orderByDesc(LeaveRequest::getCreateTime);
        return leaveRequestMapper.selectPage(page, w);
    }

    public LeaveRequest apply(LeaveRequest req) {
        req.setStatus("pending");
        leaveRequestMapper.insert(req);

        notifyTeacher(req, "学生" + getStudentName(req.getStudentId()) + "提交了请假申请");

        return req;
    }

    @Transactional
    public LeaveRequest approve(Long id, Long approverId, String remark) {
        LeaveRequest req = leaveRequestMapper.selectById(id);
        BusinessException.notNull(req, "请假申请不存在");
        BusinessException.isTrue("pending".equals(req.getStatus()), "只能审批待处理的申请");

        req.setStatus("approved");
        req.setApproverId(approverId);
        req.setApproveRemark(remark);
        req.setApproveTime(LocalDateTime.now());
        leaveRequestMapper.updateById(req);

        syncAttendanceForLeave(req);

        notifyStudent(req, "你的请假申请已通过审批");

        return req;
    }

    public LeaveRequest reject(Long id, Long approverId, String remark) {
        LeaveRequest req = leaveRequestMapper.selectById(id);
        BusinessException.notNull(req, "请假申请不存在");
        BusinessException.isTrue("pending".equals(req.getStatus()), "只能审批待处理的申请");

        req.setStatus("rejected");
        req.setApproverId(approverId);
        req.setApproveRemark(remark);
        req.setApproveTime(LocalDateTime.now());
        leaveRequestMapper.updateById(req);

        notifyStudent(req, "你的请假申请已被驳回：" + (remark != null ? remark : ""));

        return req;
    }

    private void syncAttendanceForLeave(LeaveRequest req) {
        LocalDate start = req.getStartDate();
        LocalDate end = req.getEndDate() != null ? req.getEndDate() : start;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            Attendance existing = attendanceMapper.selectOne(
                    new LambdaQueryWrapper<Attendance>()
                            .eq(Attendance::getStudentId, req.getStudentId())
                            .eq(Attendance::getCourseId, req.getCourseId())
                            .eq(Attendance::getAttendanceDate, date));

            if (existing != null) {
                existing.setStatus(AttendanceStatus.LEAVE);
                existing.setRemark("请假审批通过自动同步");
                attendanceMapper.updateById(existing);
            } else {
                Attendance a = Attendance.builder()
                        .studentId(req.getStudentId())
                        .courseId(req.getCourseId())
                        .classId(req.getClassId())
                        .attendanceDate(date)
                        .status(AttendanceStatus.LEAVE)
                        .confidence(BigDecimal.valueOf(1.0))
                        .remark("请假审批通过自动同步")
                        .build();
                attendanceMapper.insert(a);
            }
        }
    }

    private void notifyTeacher(LeaveRequest req, String content) {
        if (req.getCourseId() != null) {
            Course course = courseMapper.selectById(req.getCourseId());
            if (course != null && course.getTeacherId() != null) {
                notificationMapper.insert(Notification.builder()
                        .userId(course.getTeacherId()).type("leave").title("请假申请")
                        .content(content).refId(req.getId()).isRead(0).build());
            }
        }
    }

    private void notifyStudent(LeaveRequest req, String content) {
        Student student = studentMapper.selectById(req.getStudentId());
        if (student != null && student.getUserId() != null) {
            notificationMapper.insert(Notification.builder()
                    .userId(student.getUserId()).type("leave").title("请假审批")
                    .content(content).refId(req.getId()).isRead(0).build());
        }
    }

    private String getStudentName(Long studentId) {
        Student s = studentMapper.selectById(studentId);
        return s != null ? s.getName() : "未知";
    }
}
