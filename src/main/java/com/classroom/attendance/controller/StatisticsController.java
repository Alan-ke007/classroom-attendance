package com.classroom.attendance.controller;

import com.classroom.attendance.common.Result;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.model.Student;
import com.classroom.attendance.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计仪表盘控制器（为小程序提供数据）
 */
@Slf4j
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 获取仪表盘概览统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        try {
            Map<String, Object> stats = statisticsService.getDashboardStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取考勤统计
     */
    @GetMapping("/attendance")
    public Result<Map<String, Object>> getAttendanceStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr == null) throw new RuntimeException("无法获取用户身份信息");
            Long userId = userIdAttr instanceof Long ? (Long) userIdAttr : ((Number) userIdAttr).longValue();
            Long filterStudentId = null;
            List<Long> filterClassIds = null;

            if ("student".equals(role)) {
                Student student = studentMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, userId));
                if (student != null) {
                    filterStudentId = student.getId();
                }
            } else if ("teacher".equals(role)) {
                List<Course> teacherCourses = courseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, userId)
                        .select(Course::getClassId));
                filterClassIds = teacherCourses.stream()
                    .map(Course::getClassId)
                    .distinct()
                    .collect(Collectors.toList());
            }

            Map<String, Object> stats = statisticsService.getAttendanceStats(startDate, endDate, filterStudentId, filterClassIds);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取考勤统计数据失败", e);
            return Result.error("获取考勤统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生个人仪表盘统计（仅该学生自己的数据）
     */
    @GetMapping("/student/{studentId}")
    public Result<Map<String, Object>> getStudentDashboardStats(@PathVariable Long studentId) {
        try {
            Map<String, Object> stats = statisticsService.getStudentDashboardStats(studentId);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取学生仪表盘统计失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生本周周报
     */
    @GetMapping("/weekly-report/{studentId}")
    public Result<Map<String, Object>> getWeeklyReport(@PathVariable Long studentId) {
        try {
            Map<String, Object> report = statisticsService.getWeeklyReport(studentId);
            return Result.success(report);
        } catch (Exception e) {
            log.error("获取学生周报失败", e);
            return Result.error("获取周报失败: " + e.getMessage());
        }
    }

    /**
     * 获取课堂质量综合评分
     */
    @GetMapping("/class-quality")
    public Result<Map<String, Object>> getClassQuality() {
        try {
            Map<String, Object> quality = statisticsService.getClassQuality();
            return Result.success(quality);
        } catch (Exception e) {
            log.error("获取课堂质量评分失败", e);
            return Result.error("获取课堂质量评分失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生考勤率排行
     */
    @GetMapping("/ranking")
    public Result<Map<String, Object>> getStudentAttendanceRanking(
            @RequestParam(required = false) Long classId) {
        try {
            Map<String, Object> ranking = statisticsService.getStudentAttendanceRanking(classId);
            return Result.success(ranking);
        } catch (Exception e) {
            log.error("获取学生考勤率排行失败", e);
            return Result.error("获取学生考勤率排行失败: " + e.getMessage());
        }
    }
}
