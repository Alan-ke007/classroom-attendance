package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.OperationLog;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.mapper.ClassMapper;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.model.Attendance;
import com.classroom.attendance.model.ClassInfo;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.model.Student;
import com.classroom.attendance.service.AttendanceService;
import com.classroom.attendance.utils.ExcelExportUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private CourseMapper courseMapper;
    
    /**
     * 分页查询考勤记录列表（按角色过滤：学生只看自己的，教师看所教班级的，管理员看全部）
     */
    @GetMapping("/list")
    public Result<Page<Attendance>> getAttendanceList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        log.info("接收到分页查询考勤记录请求");

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
                // 教师只看到自己所教班级的考勤
                List<Course> teacherCourses = courseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, userId)
                        .select(Course::getClassId));
                filterClassIds = teacherCourses.stream()
                    .map(Course::getClassId)
                    .distinct()
                    .collect(Collectors.toList());
            }

            Page<Attendance> page = attendanceService.getAttendanceList(pageNum, pageSize, filterStudentId, filterClassIds);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询考勤记录列表失败", e);
            return Result.error("查询考勤记录列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询考勤记录详情
     */
    @GetMapping("/{id}")
    public Result<Attendance> getAttendanceById(@PathVariable Long id) {
        log.info("接收到查询考勤记录详情请求，ID: {}", id);
        
        try {
            Attendance attendance = attendanceService.getAttendanceById(id);
            if (attendance != null) {
                return Result.success(attendance);
            } else {
                return Result.error("考勤记录不存在");
            }
        } catch (Exception e) {
            log.error("查询考勤记录详情失败", e);
            return Result.error("查询考勤记录详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据学生ID查询考勤记录
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Attendance>> getAttendancesByStudentId(@PathVariable Long studentId) {
        log.info("接收到根据学生ID查询考勤记录请求");
        
        try {
            List<Attendance> attendances = attendanceService.getAttendancesByStudentId(studentId);
            return Result.success(attendances);
        } catch (Exception e) {
            log.error("查询学生考勤记录失败", e);
            return Result.error("查询学生考勤记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据班级ID查询考勤记录
     */
    @GetMapping("/class/{classId}")
    public Result<List<Attendance>> getAttendancesByClassId(@PathVariable Long classId) {
        log.info("接收到根据班级ID查询考勤记录请求");
        
        try {
            List<Attendance> attendances = attendanceService.getAttendancesByClassId(classId);
            return Result.success(attendances);
        } catch (Exception e) {
            log.error("查询班级考勤记录失败", e);
            return Result.error("查询班级考勤记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据课程ID查询考勤记录
     */
    @GetMapping("/course/{courseId}")
    public Result<List<Attendance>> getAttendancesByCourseId(@PathVariable Long courseId) {
        log.info("接收到根据课程ID查询考勤记录请求");
        
        try {
            List<Attendance> attendances = attendanceService.getAttendancesByCourseId(courseId);
            return Result.success(attendances);
        } catch (Exception e) {
            log.error("查询课程考勤记录失败", e);
            return Result.error("查询课程考勤记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据日期范围查询考勤记录
     */
    @GetMapping("/range")
    public Result<List<Attendance>> getAttendancesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("接收到根据日期范围查询考勤记录请求");
        
        try {
            List<Attendance> attendances = attendanceService.getAttendancesByDateRange(startDate, endDate);
            return Result.success(attendances);
        } catch (Exception e) {
            log.error("查询日期范围考勤记录失败", e);
            return Result.error("查询日期范围考勤记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算学生出勤率
     */
    @GetMapping("/rate/{studentId}")
    public Result<Double> calculateAttendanceRate(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("接收到计算出勤率请求");
        
        try {
            Double rate = attendanceService.calculateAttendanceRate(studentId, startDate, endDate);
            return Result.success(rate);
        } catch (Exception e) {
            log.error("计算出勤率失败", e);
            return Result.error("计算出勤率失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加考勤记录
     */
    @OperationLog(title = "添加考勤记录", operation = "create")
    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addAttendance(@RequestBody Attendance attendance) {
        log.info("接收到添加考勤记录请求");
        
        try {
            boolean success = attendanceService.addAttendance(attendance);
            if (success) {
                return Result.success("添加考勤记录成功");
            } else {
                return Result.error("添加考勤记录失败");
            }
        } catch (Exception e) {
            log.error("添加考勤记录失败", e);
            return Result.error("添加考勤记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新考勤记录
     */
    @OperationLog(title = "更新考勤记录", operation = "update")
    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateAttendance(@PathVariable Long id, @RequestBody Attendance attendance) {
        log.info("接收到更新考勤记录请求，ID: {}", id);
        
        try {
            attendance.setId(id);
            boolean success = attendanceService.updateAttendance(attendance);
            if (success) {
                return Result.success("更新考勤记录成功");
            } else {
                return Result.error("考勤记录不存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新考勤记录失败", e);
            return Result.error("更新考勤记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除考勤记录
     */
    @OperationLog(title = "删除考勤记录", operation = "delete")
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteAttendance(@PathVariable Long id) {
        log.info("接收到删除考勤记录请求，ID: {}", id);
        
        try {
            boolean success = attendanceService.deleteAttendance(id);
            if (success) {
                return Result.success("删除考勤记录成功");
            } else {
                return Result.error("考勤记录不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除考勤记录失败", e);
            return Result.error("删除考勤记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出考勤记录为Excel
     */
    @OperationLog(title = "导出考勤", operation = "export")
    @RequireRole({"admin", "teacher"})
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("接收到导出考勤记录请求");
        
        try {
            List<Attendance> attendances;
            if (startDate != null && endDate != null) {
                attendances = attendanceService.getAttendancesByDateRange(startDate, endDate);
            } else {
                // 获取所有考勤记录
                Page<Attendance> page = attendanceService.getAttendanceList(1, 10000, null, (Collection<Long>) null);
                attendances = page.getRecords();
            }
            
            // 预加载关联数据
            Map<Long, Student> studentMap = new HashMap<>();
            Map<Long, ClassInfo> classMap = new HashMap<>();
            Map<Long, Course> courseMap = new HashMap<>();
            for (Attendance a : attendances) {
                if (a.getStudentId() != null && !studentMap.containsKey(a.getStudentId())) {
                    Student s = studentMapper.selectById(a.getStudentId());
                    if (s != null) studentMap.put(a.getStudentId(), s);
                }
                if (a.getClassId() != null && !classMap.containsKey(a.getClassId())) {
                    ClassInfo c = classMapper.selectById(a.getClassId());
                    if (c != null) classMap.put(a.getClassId(), c);
                }
                if (a.getCourseId() != null && !courseMap.containsKey(a.getCourseId())) {
                    Course c = courseMapper.selectById(a.getCourseId());
                    if (c != null) courseMap.put(a.getCourseId(), c);
                }
            }

            // 转换为Excel数据格式
            String[] headers = {"学号", "姓名", "班级", "课程", "考勤日期", "状态", "签到时间", "置信度", "备注"};
            List<Map<String, Object>> data = attendances.stream().map(attendance -> {
                Map<String, Object> row = new HashMap<>();
                Student s = studentMap.get(attendance.getStudentId());
                ClassInfo ci = classMap.get(attendance.getClassId());
                Course co = courseMap.get(attendance.getCourseId());
                row.put("studentNo", s != null ? s.getStudentNo() : "未知");
                row.put("name", s != null ? s.getName() : "未知");
                row.put("className", ci != null ? ci.getClassName() : "未知");
                row.put("courseName", co != null ? co.getCourseName() : "未知");
                row.put("attendanceDate", attendance.getAttendanceDate());
                row.put("status", getStatusText(attendance.getStatus()));
                row.put("checkInTime", attendance.getCheckInTime());
                row.put("confidence", attendance.getConfidence());
                row.put("remark", attendance.getRemark());
                return row;
            }).collect(Collectors.toList());
            
            // 生成Excel文件
            byte[] excelData = ExcelExportUtil.exportExcel(headers, data, "考勤记录");
            
            // 设置响应头
            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = URLEncoder.encode("考勤记录_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentDispositionFormData("attachment", fileName);
            
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .body(excelData);
        } catch (IOException e) {
            log.error("导出考勤记录失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private String getStatusText(String status) {
        return switch (status) {
            case "present" -> "出勤";
            case "absent" -> "缺勤";
            case "late" -> "迟到";
            case "leave" -> "请假";
            default -> status;
        };
    }
}
