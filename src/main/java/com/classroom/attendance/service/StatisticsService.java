package com.classroom.attendance.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取仪表盘统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取考勤统计
     */
    Map<String, Object> getAttendanceStats(LocalDate startDate, LocalDate endDate, Long filterStudentId, Collection<Long> filterClassIds);

    /**
     * 获取学生考勤率排行
     */
    Map<String, Object> getStudentAttendanceRanking(Long classId);

    /**
     * 获取学生个人仪表盘统计（仅该学生的数据）
     */
    Map<String, Object> getStudentDashboardStats(Long studentId);

    /**
     * 获取学生本周周报数据
     */
    Map<String, Object> getWeeklyReport(Long studentId);

    /**
     * 获取课堂质量评分
     */
    Map<String, Object> getClassQuality();
}
