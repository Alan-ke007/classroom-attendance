package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.Attendance;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 考勤记录服务接口
 */
public interface AttendanceService {
    
    /**
     * 分页查询考勤记录列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Attendance> getAttendanceList(Integer pageNum, Integer pageSize, Long studentId, Collection<Long> classIds);
    
    /**
     * 根据ID查询考勤记录
     * @param id 考勤记录ID
     * @return 考勤记录信息
     */
    Attendance getAttendanceById(Long id);
    
    /**
     * 添加考勤记录
     * @param attendance 考勤记录信息
     * @return 是否成功
     */
    boolean addAttendance(Attendance attendance);
    
    /**
     * 更新考勤记录
     * @param attendance 考勤记录信息
     * @return 是否成功
     */
    boolean updateAttendance(Attendance attendance);
    
    /**
     * 删除考勤记录
     * @param id 考勤记录ID
     * @return 是否成功
     */
    boolean deleteAttendance(Long id);
    
    /**
     * 根据学生ID查询考勤记录
     * @param studentId 学生ID
     * @return 考勤记录列表
     */
    List<Attendance> getAttendancesByStudentId(Long studentId);
    
    /**
     * 根据班级ID查询考勤记录
     * @param classId 班级ID
     * @return 考勤记录列表
     */
    List<Attendance> getAttendancesByClassId(Long classId);
    
    /**
     * 根据课程ID查询考勤记录
     * @param courseId 课程ID
     * @return 考勤记录列表
     */
    List<Attendance> getAttendancesByCourseId(Long courseId);
    
    /**
     * 根据日期范围查询考勤记录
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤记录列表
     */
    List<Attendance> getAttendancesByDateRange(LocalDate startDate, LocalDate endDate);
    
    /**
     * 统计学生出勤率
     * @param studentId 学生ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 出勤率（百分比）
     */
    Double calculateAttendanceRate(Long studentId, LocalDate startDate, LocalDate endDate);
}
