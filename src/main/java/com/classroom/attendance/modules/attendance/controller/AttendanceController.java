package com.classroom.attendance.modules.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.OperationLog;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.infrastructure.util.ExcelExportUtil;
import com.classroom.attendance.modules.attendance.dto.AttendanceCreateRequest;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.enums.AttendanceStatus;
import com.classroom.attendance.modules.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController extends BaseController {

    private final AttendanceService attendanceService;

    @GetMapping("/list")
    public Result<Page<Attendance>> getAttendanceList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(attendanceService.listForCurrentUser(pageNum, pageSize, studentName, status, startDate, endDate));
    }

    @RequireRole({"admin", "teacher"}) // H1：单条考勤记录为跨租户 PII
    @GetMapping("/{id}")
    public Result<Attendance> getAttendanceById(@PathVariable Long id) {
        return Result.success(attendanceService.getById(id));
    }

    @RequireRole({"admin", "teacher"}) // H1：按学生查全量考勤（跨学生数据）
    @GetMapping("/student/{studentId}")
    public Result<List<Attendance>> getAttendancesByStudentId(@PathVariable Long studentId) {
        return Result.success(attendanceService.getByStudentId(studentId));
    }

    @RequireRole({"admin", "teacher"}) // H1：按班级查全量考勤
    @GetMapping("/class/{classId}")
    public Result<List<Attendance>> getAttendancesByClassId(@PathVariable Long classId) {
        return Result.success(attendanceService.getByClassId(classId));
    }

    @RequireRole({"admin", "teacher"}) // H1：按课程查全量考勤
    @GetMapping("/course/{courseId}")
    public Result<List<Attendance>> getAttendancesByCourseId(@PathVariable Long courseId) {
        return Result.success(attendanceService.getByCourseId(courseId));
    }

    @RequireRole({"admin", "teacher"}) // H1：按日期范围查全量考勤
    @GetMapping("/range")
    public Result<List<Attendance>> getAttendancesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(attendanceService.getByDateRange(startDate, endDate));
    }

    @RequireRole({"admin", "teacher"}) // H1：出勤率按任意 studentId 计算，属跨租户
    @GetMapping("/rate/{studentId}")
    public Result<Double> calculateAttendanceRate(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(attendanceService.calculateRate(studentId, startDate, endDate));
    }

    @OperationLog(title = "添加考勤记录", operation = "create")
    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addAttendance(@Valid @RequestBody AttendanceCreateRequest req) {
        Attendance attendance = Attendance.builder()
                .studentId(req.getStudentId()).courseId(req.getCourseId())
                .classId(req.getClassId()).attendanceDate(req.getAttendanceDate())
                .status(req.getStatus() != null ? AttendanceStatus.fromDbValue(req.getStatus()) : null)
                .remark(req.getRemark()).imagePath(req.getImagePath())
                .build();
        attendanceService.create(attendance);
        return Result.success("添加考勤记录成功");
    }

    @OperationLog(title = "更新考勤记录", operation = "update")
    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateAttendance(@PathVariable Long id, @RequestBody Attendance attendance) {
        attendanceService.update(id, attendance);
        return Result.success("更新考勤记录成功");
    }

    @OperationLog(title = "删除考勤记录", operation = "delete")
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteAttendance(@PathVariable Long id) {
        attendanceService.delete(id);
        return Result.success("删除考勤记录成功");
    }

    @OperationLog(title = "导出考勤", operation = "export")
    @RequireRole({"admin", "teacher"})
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Attendance> attendances = attendanceService.getExportData(startDate, endDate);

            String[] headers = {"学号", "姓名", "班级", "课程", "考勤日期", "状态", "签到时间", "置信度", "备注"};
            List<Map<String, Object>> data = attendances.stream()
                    .map(attendanceService::buildExportRow).collect(Collectors.toList());

            byte[] excelData = ExcelExportUtil.exportExcel(headers, data, "考勤记录");
            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = URLEncoder.encode("考勤记录_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok().headers(httpHeaders).body(excelData);
        } catch (IOException e) {
            throw new com.classroom.attendance.infrastructure.exception.BusinessException(500, "导出失败", e);
        }
    }
}
