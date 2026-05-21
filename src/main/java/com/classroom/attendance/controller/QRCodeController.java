package com.classroom.attendance.controller;

import com.classroom.attendance.common.Result;
import com.classroom.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.model.Attendance;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.model.Student;
import com.classroom.attendance.utils.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private StudentMapper studentMapper;

    /**
     * 生成签到二维码Token
     * 教师调用，生成一个5分钟有效的签到Token
     */
    @GetMapping("/generate")
    public Result<Map<String, Object>> generateQRCode(@RequestParam Long courseId) {
        log.info("生成签到二维码Token，课程ID: {}", courseId);
        try {
            Course course = courseMapper.selectById(courseId);
            if (course == null) {
                return Result.error("课程不存在");
            }

            // 生成5分钟有效的短Token
            Map<String, Object> claims = new HashMap<>();
            claims.put("courseId", courseId);
            claims.put("type", "qrcode_checkin");
            claims.put("timestamp", System.currentTimeMillis());

            String token = jwtUtil.generateToken(claims, 5 * 60 * 1000); // 5分钟

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("courseId", courseId);
            result.put("courseName", course.getCourseName());
            result.put("expireMinutes", 5);
            result.put("generateTime", LocalDateTime.now().toString());

            log.info("二维码Token生成成功: {}", token.substring(0, 20) + "...");
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成二维码Token失败", e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 扫码签到
     * 学生扫描二维码后调用此接口完成签到
     */
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String studentIdStr = body.get("studentId");
        String studentNo = body.get("studentNo");

        Long studentId = null;
        if (studentIdStr != null && !studentIdStr.isEmpty()) {
            studentId = Long.valueOf(studentIdStr);
        } else if (studentNo != null && !studentNo.isEmpty()) {
            Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo)
            );
            if (student == null) {
                return Result.error("学号不存在: " + studentNo);
            }
            studentId = student.getId();
        } else {
            return Result.error("请提供学生ID或学号");
        }

        log.info("扫码签到，学生ID: {}", studentId);

        try {
            // 验证Token
            Map<String, Object> claims = jwtUtil.parseToken(token);
            String type = (String) claims.get("type");
            if (!"qrcode_checkin".equals(type)) {
                return Result.error("无效的签到二维码");
            }

            Long courseId = ((Number) claims.get("courseId")).longValue();

            // 检查是否已签到
            LocalDate today = LocalDate.now();
            Attendance existing = attendanceMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attendance>()
                            .eq(Attendance::getStudentId, studentId)
                            .eq(Attendance::getCourseId, courseId)
                            .eq(Attendance::getAttendanceDate, today)
            );

            if (existing != null) {
                return Result.error("今日已签到，请勿重复签到");
            }

            // 创建考勤记录
            Course course = courseMapper.selectById(courseId);
            Attendance attendance = Attendance.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .classId(course != null ? course.getClassId() : null)
                    .attendanceDate(today)
                    .status("present")
                    .checkInTime(LocalDateTime.now())
                    .confidence(java.math.BigDecimal.valueOf(1.0))
                    .remark("二维码签到")
                    .build();

            attendanceMapper.insert(attendance);

            Map<String, Object> result = new HashMap<>();
            result.put("message", "签到成功");
            result.put("courseName", course != null ? course.getCourseName() : "未知课程");
            result.put("checkInTime", attendance.getCheckInTime().toString());

            log.info("学生 {} 二维码签到成功，课程: {}", studentId, courseId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("扫码签到失败: {}", e.getMessage());
            return Result.error("签到失败: " + e.getMessage());
        }
    }
}
