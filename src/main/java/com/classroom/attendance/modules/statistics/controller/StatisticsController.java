package com.classroom.attendance.modules.statistics.controller;

import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.constant.Constants;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController extends BaseController {

    private final StatisticsService statisticsService;

    @RequireRole({"admin", "teacher"}) // H1：全局聚合看板
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(statisticsService.getDashboardStats());
    }

    @RequireRole({"admin", "teacher"}) // H1：全局考勤聚合统计
    @GetMapping("/attendance")
    public Result<Map<String, Object>> getAttendanceStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getAttendanceStats(startDate, endDate));
    }

    // 归属裁剪：学生仅能看自己的看板；教师/管理员可查任意（H1 修复，关闭横向越权）
    @GetMapping("/student/{studentId}")
    public Result<Map<String, Object>> getStudentDashboardStats(@PathVariable Long studentId) {
        if (Constants.Role.STUDENT.equals(SecurityUtil.getCurrentRole())
                && !studentId.equals(SecurityUtil.getCurrentStudentId())) {
            throw new BusinessException(403, "无权查看他人看板");
        }
        return Result.success(statisticsService.getStudentDashboardStats(studentId));
    }

    // 归属裁剪：学生仅能看自己的周报；教师/管理员可查任意（H1 修复，关闭横向越权）
    @GetMapping("/weekly-report/{studentId}")
    public Result<Map<String, Object>> getWeeklyReport(@PathVariable Long studentId) {
        if (Constants.Role.STUDENT.equals(SecurityUtil.getCurrentRole())
                && !studentId.equals(SecurityUtil.getCurrentStudentId())) {
            throw new BusinessException(403, "无权查看他人周报");
        }
        return Result.success(statisticsService.getWeeklyReport(studentId));
    }

    @RequireRole({"admin", "teacher"}) // H1：班级质量聚合
    @GetMapping("/class-quality")
    public Result<Map<String, Object>> getClassQuality() {
        return Result.success(statisticsService.getClassQuality());
    }

    @RequireRole({"admin", "teacher"}) // H1：考勤排名聚合
    @GetMapping("/ranking")
    public Result<Map<String, Object>> getStudentAttendanceRanking(@RequestParam(required = false) Long classId) {
        return Result.success(statisticsService.getStudentAttendanceRanking(classId));
    }

    @RequireRole({"admin", "teacher"}) // H1：今日课表（教师/管理视角）
    @GetMapping("/today-schedule")
    public Result<Map<String, Object>> getTodaySchedule() {
        return Result.success(statisticsService.getTodaySchedule());
    }

    @RequireRole({"admin", "teacher"}) // H1：待办聚合
    @GetMapping("/pending-tasks")
    public Result<Map<String, Object>> getPendingTasks() {
        return Result.success(statisticsService.getPendingTasks());
    }

    @RequireRole({"admin", "teacher"}) // H1：学分排名聚合
    @GetMapping("/credit-ranking")
    public Result<Map<String, Object>> getCreditRanking(@RequestParam(required = false) Long classId) {
        return Result.success(statisticsService.getCreditRanking(classId));
    }
}
