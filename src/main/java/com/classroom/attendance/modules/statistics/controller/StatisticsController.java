package com.classroom.attendance.modules.statistics.controller;

import com.classroom.attendance.infrastructure.base.BaseController;
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

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(statisticsService.getDashboardStats());
    }

    @GetMapping("/attendance")
    public Result<Map<String, Object>> getAttendanceStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(statisticsService.getAttendanceStats(startDate, endDate));
    }

    @GetMapping("/student/{studentId}")
    public Result<Map<String, Object>> getStudentDashboardStats(@PathVariable Long studentId) {
        return Result.success(statisticsService.getStudentDashboardStats(studentId));
    }

    @GetMapping("/weekly-report/{studentId}")
    public Result<Map<String, Object>> getWeeklyReport(@PathVariable Long studentId) {
        return Result.success(statisticsService.getWeeklyReport(studentId));
    }

    @GetMapping("/class-quality")
    public Result<Map<String, Object>> getClassQuality() {
        return Result.success(statisticsService.getClassQuality());
    }

    @GetMapping("/ranking")
    public Result<Map<String, Object>> getStudentAttendanceRanking(@RequestParam(required = false) Long classId) {
        return Result.success(statisticsService.getStudentAttendanceRanking(classId));
    }

    @GetMapping("/today-schedule")
    public Result<Map<String, Object>> getTodaySchedule() {
        return Result.success(statisticsService.getTodaySchedule());
    }

    @GetMapping("/pending-tasks")
    public Result<Map<String, Object>> getPendingTasks() {
        return Result.success(statisticsService.getPendingTasks());
    }

    @GetMapping("/credit-ranking")
    public Result<Map<String, Object>> getCreditRanking(@RequestParam(required = false) Long classId) {
        return Result.success(statisticsService.getCreditRanking(classId));
    }
}
