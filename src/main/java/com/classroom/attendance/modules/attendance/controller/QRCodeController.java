package com.classroom.attendance.modules.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.infrastructure.util.JwtUtil;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.enums.AttendanceStatus;
import com.classroom.attendance.modules.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.student.service.CreditScoreService;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/qrcode")
@RequiredArgsConstructor
public class QRCodeController extends BaseController {

    private final JwtUtil jwtUtil;
    private final CourseMapper courseMapper;
    private final AttendanceMapper attendanceMapper;
    private final CreditScoreService creditScoreService;

    private static final String[] WEEKDAY_NAMES = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    @GetMapping("/generate")
    public Result<Map<String, Object>> generateQRCode(@RequestParam Long courseId) {
        Course course = courseMapper.selectById(courseId);
        BusinessException.notNull(course, "课程不存在");

        LocalDate today = LocalDate.now();
        String todayWeekDay = WEEKDAY_NAMES[today.getDayOfWeek().getValue() - 1];
        if (!todayWeekDay.equals(course.getWeekDay())) {
            throw new BusinessException("今天不是该课程的上课日（" + course.getWeekDay() + "）");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("courseId", courseId);
        claims.put("type", "qrcode_checkin");
        claims.put("timestamp", System.currentTimeMillis());

        // 二维码有效期：课程开始前10分钟 ~ 课程结束后10分钟
        long validFrom = System.currentTimeMillis() - 10 * 60 * 1000;
        long expireMs = (course.getEndTime().toSecondOfDay() + 10 * 60) * 1000L
                - LocalTime.now().toSecondOfDay() * 1000L;
        if (expireMs <= 0) expireMs = 5 * 60 * 1000;
        String token = jwtUtil.generateToken(claims, expireMs);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("courseId", courseId);
        result.put("courseName", course.getCourseName());
        result.put("startTime", course.getStartTime().toString());
        result.put("endTime", course.getEndTime().toString());
        result.put("validFrom", LocalDateTime.now().minusMinutes(10).toString());
        result.put("generateTime", LocalDateTime.now().toString());
        return Result.success(result);
    }

    @Transactional
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        // 安全(H2)：签到主体以服务端当前登录学生身份为准，忽略请求体 studentId/studentNo，防伪造签到。
        Long studentId = SecurityUtil.getCurrentStudentId();
        BusinessException.notNull(studentId, "未获取到学生身份，请先登录");

        Map<String, Object> claims = jwtUtil.parseToken(token);
        BusinessException.isTrue("qrcode_checkin".equals(claims.get("type")), "无效的签到二维码");
        // TODO 安全(H2)：当前 QR token 仅绑定 courseId，应进一步绑定 studentId/classId、单次使用、短时效（P1）。

        Long courseId = ((Number) claims.get("courseId")).longValue();
        LocalDate today = LocalDate.now();

        Attendance existing = attendanceMapper.selectOne(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStudentId, studentId)
                        .eq(Attendance::getCourseId, courseId)
                        .eq(Attendance::getAttendanceDate, today));
        BusinessException.isTrue(existing == null, "今日已签到，请勿重复签到");

        Course course = courseMapper.selectById(courseId);
        BusinessException.notNull(course, "课程不存在");

        LocalTime now = LocalTime.now();
        LocalTime startTime = course.getStartTime();
        AttendanceStatus status = determineStatus(now, startTime);

        Attendance attendance = Attendance.builder()
                .studentId(studentId).courseId(courseId)
                .classId(course.getClassId())
                .attendanceDate(today).status(status)
                .checkInTime(LocalDateTime.now())
                .confidence(BigDecimal.valueOf(1.0)).remark(status == AttendanceStatus.PRESENT ? "正常签到" : "迟到签到").build();

        attendanceMapper.insert(attendance);

        if (status == AttendanceStatus.PRESENT) creditScoreService.addAttendancePresent(studentId);
        else if (status == AttendanceStatus.LATE) creditScoreService.addAttendanceLate(studentId);
        else if (status == AttendanceStatus.ABSENT) creditScoreService.addAttendanceAbsent(studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("message", status == AttendanceStatus.PRESENT ? "签到成功" : "迟到签到");
        result.put("status", status.getDescription());
        result.put("courseName", course.getCourseName());
        result.put("checkInTime", attendance.getCheckInTime().toString());
        return Result.success(result);
    }

    private AttendanceStatus determineStatus(LocalTime now, LocalTime courseStart) {
        long minutesAfterStart = now.toSecondOfDay() - courseStart.toSecondOfDay();
        if (minutesAfterStart < 0) minutesAfterStart = minutesAfterStart / 60;
        else minutesAfterStart = minutesAfterStart / 60;

        if (minutesAfterStart <= 10) return AttendanceStatus.PRESENT;
        if (minutesAfterStart <= 30) return AttendanceStatus.LATE;
        return AttendanceStatus.ABSENT;
    }

    @GetMapping("/courses/today")
    public Result<List<Map<String, Object>>> getTodayCourses() {
        String todayWeekDay = WEEKDAY_NAMES[LocalDate.now().getDayOfWeek().getValue() - 1];
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getWeekDay, todayWeekDay)
                        .orderByAsc(Course::getStartTime));

        List<Map<String, Object>> result = courses.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("courseName", c.getCourseName());
            m.put("classId", c.getClassId());
            m.put("classroom", c.getClassroom());
            m.put("startTime", c.getStartTime().toString());
            m.put("endTime", c.getEndTime().toString());
            m.put("teacherId", c.getTeacherId());
            // 统计今日签到情况
            long checkedIn = attendanceMapper.selectCount(
                    new LambdaQueryWrapper<Attendance>()
                            .eq(Attendance::getCourseId, c.getId())
                            .eq(Attendance::getAttendanceDate, LocalDate.now()));
            m.put("checkedInCount", checkedIn);
            return m;
        }).toList();

        return Result.success(result);
    }
}
