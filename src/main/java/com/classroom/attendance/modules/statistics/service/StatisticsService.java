package com.classroom.attendance.modules.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.modules.behavior.entity.BehaviorRecord;
import com.classroom.attendance.modules.behavior.mapper.BehaviorRecordMapper;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.leave.mapper.LeaveRequestMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final ClassMapper classMapper;
    private final AttendanceMapper attendanceMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;
    private final LeaveRequestMapper leaveRequestMapper;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        String role = SecurityUtil.getCurrentRole();
        Long userId = SecurityUtil.getCurrentUserId();

        List<Long> teacherClassIds = null;
        if ("teacher".equals(role)) {
            teacherClassIds = classMapper.selectList(
                    new LambdaQueryWrapper<com.classroom.attendance.modules.classmgmt.entity.ClassInfo>()
                            .eq(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getTeacher, SecurityUtil.getCurrentUserRealName()))
                    .stream().map(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getId).toList();
        }

        long studentCount;
        long courseCount;
        long classCount;
        if (teacherClassIds != null && teacherClassIds.isEmpty()) {
            studentCount = 0;
            courseCount = 0;
            classCount = 0;
        } else {
            LambdaQueryWrapper<Student> studentW = new LambdaQueryWrapper<>();
            if (teacherClassIds != null) studentW.in(Student::getClassId, teacherClassIds);
            studentCount = studentMapper.selectCount(studentW);

            LambdaQueryWrapper<Course> courseW = new LambdaQueryWrapper<>();
            if ("teacher".equals(role)) courseW.eq(Course::getTeacherId, userId);
            courseCount = courseMapper.selectCount(courseW);

            classCount = teacherClassIds != null ? teacherClassIds.size() : classMapper.selectCount(null);
        }

        LambdaQueryWrapper<Attendance> attendanceW = new LambdaQueryWrapper<>();
        if (teacherClassIds != null) attendanceW.in(Attendance::getClassId, teacherClassIds);
        long totalAttendance = attendanceMapper.selectCount(attendanceW);

        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Attendance> todayW = new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getAttendanceDate, today);
        if (teacherClassIds != null) todayW.in(Attendance::getClassId, teacherClassIds);
        long todayAttendance = attendanceMapper.selectCount(todayW);

        LambdaQueryWrapper<Attendance> presentW = new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getStatus, "present");
        if (teacherClassIds != null) presentW.in(Attendance::getClassId, teacherClassIds);
        long presentCount = attendanceMapper.selectCount(presentW);

        LambdaQueryWrapper<BehaviorRecord> behaviorW = new LambdaQueryWrapper<BehaviorRecord>()
                .eq(BehaviorRecord::getHandled, 0);
        if (teacherClassIds != null) behaviorW.in(BehaviorRecord::getClassId, teacherClassIds);
        long unhandledBehavior = behaviorRecordMapper.selectCount(behaviorW);

        stats.put("studentCount", studentCount);
        stats.put("courseCount", courseCount);
        stats.put("classCount", classCount);
        stats.put("totalAttendance", totalAttendance);
        stats.put("todayAttendance", todayAttendance);
        stats.put("presentCount", presentCount);
        stats.put("unhandledBehavior", unhandledBehavior);

        if (totalAttendance > 0) {
            double rate = BigDecimal.valueOf(presentCount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAttendance), 1, RoundingMode.HALF_UP).doubleValue();
            stats.put("attendanceRate", rate);
        } else {
            stats.put("attendanceRate", 100.0);
        }
        return stats;
    }

    public Map<String, Object> getAttendanceStats(LocalDate startDate, LocalDate endDate) {
        String role = SecurityUtil.getCurrentRole();
        Long userId = SecurityUtil.getCurrentUserId();

        Long filterStudentId = null;
        List<Long> filterClassIds = null;

        if ("student".equals(role)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
            if (student != null) filterStudentId = student.getId();
        } else if ("teacher".equals(role)) {
            filterClassIds = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, userId).select(Course::getClassId))
                    .stream().map(Course::getClassId).distinct().collect(Collectors.toList());
        }

        return computeAttendanceStats(startDate, endDate, filterStudentId, filterClassIds);
    }

    private Map<String, Object> computeAttendanceStats(LocalDate startDate, LocalDate endDate,
                                                        Long filterStudentId, Collection<Long> filterClassIds) {
        Map<String, Object> result = new LinkedHashMap<>();
        LambdaQueryWrapper<Attendance> w = new LambdaQueryWrapper<>();
        if (startDate != null) w.ge(Attendance::getAttendanceDate, startDate);
        if (endDate != null) w.le(Attendance::getAttendanceDate, endDate);
        if (filterStudentId != null) w.eq(Attendance::getStudentId, filterStudentId);
        if (!CollectionUtils.isEmpty(filterClassIds)) w.in(Attendance::getClassId, filterClassIds);

        List<Attendance> all = attendanceMapper.selectList(w);

        long present = all.stream().filter(a -> "present".equals(a.getStatus())).count();
        long late = all.stream().filter(a -> "late".equals(a.getStatus())).count();
        long absent = all.stream().filter(a -> "absent".equals(a.getStatus())).count();
        long leave = all.stream().filter(a -> "leave".equals(a.getStatus())).count();

        result.put("total", all.size());
        result.put("present", present);
        result.put("late", late);
        result.put("absent", absent);
        result.put("leave", leave);

        Map<LocalDate, Long> dailyCount = all.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate, TreeMap::new, Collectors.counting()));
        result.put("daily", dailyCount);
        return result;
    }

    public Map<String, Object> getStudentAttendanceRanking(Long classId) {
        Map<String, Object> result = new LinkedHashMap<>();
        LambdaQueryWrapper<Attendance> w = new LambdaQueryWrapper<>();
        if (classId != null) w.eq(Attendance::getClassId, classId);

        List<Attendance> list = attendanceMapper.selectList(w);
        Map<Long, List<Attendance>> grouped = list.stream().collect(Collectors.groupingBy(Attendance::getStudentId));

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            long total = entry.getValue().size();
            long present = entry.getValue().stream().filter(a -> "present".equals(a.getStatus())).count();
            double rate = total > 0 ? BigDecimal.valueOf(present).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP).doubleValue() : 0;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentId", entry.getKey());
            item.put("total", total);
            item.put("present", present);
            item.put("rate", rate);
            ranking.add(item);
        }
        ranking.sort((a, b) -> Double.compare((Double) b.get("rate"), (Double) a.get("rate")));
        result.put("ranking", ranking);
        result.put("total", ranking.size());
        return result;
    }

    public Map<String, Object> getStudentDashboardStats(Long studentId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        String todayWeekDay = new String[]{"周日", "周一", "周二", "周三", "周四", "周五", "周六"}[today.getDayOfWeek().getValue() % 7];

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            stats.put("totalCourses", 0); stats.put("presentCount", 0); stats.put("absentCount", 0);
            stats.put("todayCourses", 0); stats.put("attendanceRate", 100.0);
            return stats;
        }

        long totalCourses = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getClassId, student.getClassId()));
        long todayCourses = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getClassId, student.getClassId()).eq(Course::getWeekDay, todayWeekDay));

        List<Attendance> attendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>().eq(Attendance::getStudentId, studentId));
        long presentCount = attendances.stream().filter(a -> "present".equals(a.getStatus())).count();
        long absentCount = attendances.stream().filter(a -> "absent".equals(a.getStatus())).count();
        long totalAttendance = attendances.size();

        double rate = 100.0;
        if (totalAttendance > 0) {
            rate = BigDecimal.valueOf(presentCount + attendances.stream().filter(a -> "late".equals(a.getStatus())).count())
                    .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(totalAttendance), 1, RoundingMode.HALF_UP).doubleValue();
        }

        stats.put("totalCourses", totalCourses); stats.put("todayCourses", todayCourses);
        stats.put("presentCount", presentCount); stats.put("absentCount", absentCount);
        stats.put("totalAttendance", totalAttendance); stats.put("attendanceRate", rate);
        stats.put("studentName", student.getName()); stats.put("studentNo", student.getStudentNo());
        return stats;
    }

    public Map<String, Object> getWeeklyReport(Long studentId) {
        Map<String, Object> report = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);

        report.put("weekStart", monday.toString());
        report.put("weekEnd", sunday.toString());

        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            report.put("attendanceRate", 0); report.put("totalClasses", 0); report.put("presentCount", 0);
            report.put("handRaiseCount", 0); report.put("violationCount", 0);
            report.put("violationDetails", Map.of()); report.put("trend", List.of());
            report.put("suggestion", "暂无数据，请先完成课程签到");
            return report;
        }

        List<Attendance> weekAttendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>().eq(Attendance::getStudentId, studentId)
                        .ge(Attendance::getAttendanceDate, monday).le(Attendance::getAttendanceDate, sunday));
        int total = weekAttendances.size();
        long present = weekAttendances.stream().filter(a -> "present".equals(a.getStatus())).count();
        long late = weekAttendances.stream().filter(a -> "late".equals(a.getStatus())).count();
        long absent = weekAttendances.stream().filter(a -> "absent".equals(a.getStatus())).count();

        double attendanceRate = total > 0 ? BigDecimal.valueOf(present + late).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP).doubleValue() : 100.0;

        report.put("totalClasses", total); report.put("presentCount", present);
        report.put("lateCount", late); report.put("absentCount", absent);
        report.put("attendanceRate", attendanceRate);

        List<BehaviorRecord> weekBehaviors = behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getStudentId, studentId)
                        .ge(BehaviorRecord::getBehaviorTime, monday.atStartOfDay())
                        .le(BehaviorRecord::getBehaviorTime, sunday.atTime(23, 59, 59)));

        // Also include AI-detected behaviors tagged with the student's class
        if (student.getClassId() != null) {
            List<BehaviorRecord> classBehaviors = behaviorRecordMapper.selectList(
                    new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getClassId, student.getClassId())
                            .isNull(BehaviorRecord::getStudentId)
                            .ge(BehaviorRecord::getBehaviorTime, monday.atStartOfDay())
                            .le(BehaviorRecord::getBehaviorTime, sunday.atTime(23, 59, 59)));
            weekBehaviors.addAll(classBehaviors);
        }

        Map<String, Long> behaviorCounts = weekBehaviors.stream()
                .collect(Collectors.groupingBy(BehaviorRecord::getBehaviorType, Collectors.counting()));

        long handRaiseCount = behaviorCounts.getOrDefault("raising_hand", 0L);
        Map<String, Long> violations = new LinkedHashMap<>();
        violations.put("using_phone", behaviorCounts.getOrDefault("using_phone", 0L));
        violations.put("bowing_head", behaviorCounts.getOrDefault("bowing_head", 0L));
        violations.put("leaning_over", behaviorCounts.getOrDefault("leaning_over", 0L));
        long violationCount = violations.values().stream().mapToLong(Long::longValue).sum();

        report.put("handRaiseCount", handRaiseCount); report.put("violationCount", violationCount);
        report.put("violationDetails", violations);

        Map<LocalDate, Long> dailyHandRaise = weekBehaviors.stream()
                .filter(b -> "raising_hand".equals(b.getBehaviorType()))
                .collect(Collectors.groupingBy(b -> b.getBehaviorTime().toLocalDate(), TreeMap::new, Collectors.counting()));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", d.toString());
            point.put("count", dailyHandRaise.getOrDefault(d, 0L).intValue());
            trend.add(point);
        }
        report.put("trend", trend);
        report.put("suggestion", generateSuggestion(attendanceRate, handRaiseCount, violationCount, present, absent));
        return report;
    }

    public Map<String, Object> getClassQuality() {
        Map<String, Object> quality = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        String role = SecurityUtil.getCurrentRole();
        List<Long> teacherClassIds = null;
        if ("teacher".equals(role)) {
            teacherClassIds = classMapper.selectList(
                    new LambdaQueryWrapper<com.classroom.attendance.modules.classmgmt.entity.ClassInfo>()
                            .eq(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getTeacher, SecurityUtil.getCurrentUserRealName()))
                    .stream().map(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getId).toList();
        }

        LambdaQueryWrapper<Attendance> attendanceQW = new LambdaQueryWrapper<Attendance>()
                .ge(Attendance::getAttendanceDate, weekStart).le(Attendance::getAttendanceDate, today);
        if (teacherClassIds != null) attendanceQW.in(Attendance::getClassId, teacherClassIds);
        List<Attendance> weekAttendances = attendanceMapper.selectList(attendanceQW);
        int totalAttendance = weekAttendances.size();
        long presentCount = weekAttendances.stream().filter(a -> "present".equals(a.getStatus())).count();
        long lateCount = weekAttendances.stream().filter(a -> "late".equals(a.getStatus())).count();
        long absentCount = weekAttendances.stream().filter(a -> "absent".equals(a.getStatus())).count();

        double attendanceRate = totalAttendance > 0 ? BigDecimal.valueOf(presentCount + lateCount).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalAttendance), 1, RoundingMode.HALF_UP).doubleValue() : 100.0;

        LocalDateTime weekStartTime = weekStart.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);
        LambdaQueryWrapper<BehaviorRecord> behaviorQW = new LambdaQueryWrapper<BehaviorRecord>()
                .ge(BehaviorRecord::getBehaviorTime, weekStartTime)
                .le(BehaviorRecord::getBehaviorTime, todayEnd);
        if (teacherClassIds != null) behaviorQW.in(BehaviorRecord::getClassId, teacherClassIds);
        List<BehaviorRecord> weekBehaviors = behaviorRecordMapper.selectList(behaviorQW);
        long totalBehaviors = weekBehaviors.size();

        long positiveBehaviors = weekBehaviors.stream()
                .filter(b -> List.of("raising_hand", "reading", "writing").contains(b.getBehaviorType())).count();
        long violationBehaviors = weekBehaviors.stream()
                .filter(b -> List.of("using_phone", "bowing_head", "leaning_over").contains(b.getBehaviorType())).count();

        double attendanceScore = attendanceRate;
        double handRaiseRate = totalBehaviors > 0 ? (double) weekBehaviors.stream()
                .filter(b -> "raising_hand".equals(b.getBehaviorType())).count() / totalBehaviors * 100 : 0;
        double handRaiseScore = Math.min(handRaiseRate * 5, 100);
        double violationRate = totalBehaviors > 0 ? (double) violationBehaviors / totalBehaviors * 100 : 0;
        double violationScore = Math.max(100 - violationRate * 5, 0);
        double focusRate = totalBehaviors > 0 ? (double) positiveBehaviors / totalBehaviors * 100 : 100;
        double focusScore = focusRate;
        double overallScore = attendanceScore * 0.30 + handRaiseScore * 0.25 + violationScore * 0.25 + focusScore * 0.20;
        overallScore = BigDecimal.valueOf(overallScore).setScale(1, RoundingMode.HALF_UP).doubleValue();

        quality.put("overallScore", overallScore);
        quality.put("attendanceScore", BigDecimal.valueOf(attendanceScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        quality.put("handRaiseScore", BigDecimal.valueOf(handRaiseScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        quality.put("violationScore", BigDecimal.valueOf(violationScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        quality.put("focusScore", BigDecimal.valueOf(focusScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        quality.put("totalAttendance", totalAttendance); quality.put("presentCount", presentCount);
        quality.put("lateCount", lateCount); quality.put("absentCount", absentCount);
        quality.put("attendanceRate", attendanceRate); quality.put("totalBehaviors", totalBehaviors);
        quality.put("positiveBehaviors", positiveBehaviors); quality.put("violationBehaviors", violationBehaviors);
        quality.put("weightAttendance", "30%"); quality.put("weightHandRaise", "25%");
        quality.put("weightViolation", "25%"); quality.put("weightFocus", "20%");
        return quality;
    }

    public Map<String, Object> getTodaySchedule() {
        Map<String, Object> result = new LinkedHashMap<>();
        String role = SecurityUtil.getCurrentRole();
        Long userId = SecurityUtil.getCurrentUserId();
        LocalDate today = LocalDate.now();
        String todayWeekDay = new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"}[today.getDayOfWeek().getValue() - 1];

        LambdaQueryWrapper<Course> courseW = new LambdaQueryWrapper<Course>().eq(Course::getWeekDay, todayWeekDay);
        if ("teacher".equals(role)) {
            courseW.eq(Course::getTeacherId, userId);
        } else if ("student".equals(role)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
            if (student != null && student.getClassId() != null) {
                courseW.eq(Course::getClassId, student.getClassId());
            } else {
                result.put("today", today.toString());
                result.put("weekDay", todayWeekDay);
                result.put("totalCourses", 0);
                result.put("schedule", List.of());
                return result;
            }
        }
        courseW.orderByAsc(Course::getStartTime);
        List<Course> courses = courseMapper.selectList(courseW);

        List<Map<String, Object>> schedule = courses.stream().map(c -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("courseName", c.getCourseName());
            item.put("classId", c.getClassId());
            item.put("classroom", c.getClassroom());
            item.put("startTime", c.getStartTime() != null ? c.getStartTime().toString() : "");
            item.put("endTime", c.getEndTime() != null ? c.getEndTime().toString() : "");
            if (c.getClassId() != null) {
                var ci = classMapper.selectById(c.getClassId());
                item.put("className", ci != null ? ci.getClassName() : "");
                item.put("studentCount", ci != null ? ci.getStudentCount() : 0);
            }

            long checkedIn = attendanceMapper.selectCount(
                    new LambdaQueryWrapper<Attendance>().eq(Attendance::getCourseId, c.getId())
                            .eq(Attendance::getAttendanceDate, today));
            long present = attendanceMapper.selectCount(
                    new LambdaQueryWrapper<Attendance>().eq(Attendance::getCourseId, c.getId())
                            .eq(Attendance::getAttendanceDate, today).eq(Attendance::getStatus, "present"));
            long late = attendanceMapper.selectCount(
                    new LambdaQueryWrapper<Attendance>().eq(Attendance::getCourseId, c.getId())
                            .eq(Attendance::getAttendanceDate, today).eq(Attendance::getStatus, "late"));
            item.put("checkedIn", checkedIn);
            item.put("present", present);
            item.put("late", late);
            item.put("absent", item.get("studentCount") != null ? (int) item.get("studentCount") - (int) present - (int) late - (int) checkedIn : 0);
            return item;
        }).toList();

        result.put("today", today.toString());
        result.put("weekDay", todayWeekDay);
        result.put("totalCourses", schedule.size());
        result.put("schedule", schedule);
        return result;
    }

    public Map<String, Object> getPendingTasks() {
        Map<String, Object> result = new LinkedHashMap<>();
        String role = SecurityUtil.getCurrentRole();
        Long userId = SecurityUtil.getCurrentUserId();

        long pendingLeaveCount = 0;
        if ("student".equals(role)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
            if (student != null) {
                pendingLeaveCount = leaveRequestMapper.selectCount(
                        new LambdaQueryWrapper<com.classroom.attendance.modules.leave.entity.LeaveRequest>()
                                .eq(com.classroom.attendance.modules.leave.entity.LeaveRequest::getStudentId, student.getId())
                                .eq(com.classroom.attendance.modules.leave.entity.LeaveRequest::getStatus, "pending"));
            }
        } else {
            List<Long> teacherClassIds = null;
            if ("teacher".equals(role)) {
                teacherClassIds = classMapper.selectList(
                        new LambdaQueryWrapper<com.classroom.attendance.modules.classmgmt.entity.ClassInfo>()
                                .eq(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getTeacher, SecurityUtil.getCurrentUserRealName()))
                        .stream().map(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getId).toList();
            }

            LambdaQueryWrapper<com.classroom.attendance.modules.leave.entity.LeaveRequest> leaveW =
                    new LambdaQueryWrapper<com.classroom.attendance.modules.leave.entity.LeaveRequest>()
                            .eq(com.classroom.attendance.modules.leave.entity.LeaveRequest::getStatus, "pending");
            if (teacherClassIds != null && !teacherClassIds.isEmpty()) {
                leaveW.in(com.classroom.attendance.modules.leave.entity.LeaveRequest::getClassId, teacherClassIds);
            } else if (teacherClassIds != null) {
                pendingLeaveCount = 0;
                result.put("pendingLeaveCount", 0L);
                result.put("lowAttendanceStudents", List.of());
                return result;
            }
            pendingLeaveCount = leaveRequestMapper.selectCount(leaveW);
        }

        List<Map<String, Object>> lowAttendanceStudents = new ArrayList<>();
        if ("teacher".equals(role)) {
            List<Long> teacherClassIds = classMapper.selectList(
                    new LambdaQueryWrapper<com.classroom.attendance.modules.classmgmt.entity.ClassInfo>()
                            .eq(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getTeacher, SecurityUtil.getCurrentUserRealName()))
                    .stream().map(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getId).toList();

            if (teacherClassIds != null && !teacherClassIds.isEmpty()) {
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>().in(Student::getClassId, teacherClassIds));
                for (Student s : students) {
                    List<Attendance> records = attendanceMapper.selectList(
                            new LambdaQueryWrapper<Attendance>().eq(Attendance::getStudentId, s.getId()));
                    if (records.isEmpty()) continue;
                    long present = records.stream().filter(a -> "present".equals(a.getStatus()) || "late".equals(a.getStatus())).count();
                    double rate = (double) present / records.size() * 100;
                    if (rate < 80) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("studentId", s.getId());
                        item.put("studentName", s.getName());
                        item.put("studentNo", s.getStudentNo());
                        item.put("rate", Math.round(rate * 10) / 10.0);
                        item.put("total", records.size());
                        lowAttendanceStudents.add(item);
                    }
                }
            }
        }

        result.put("pendingLeaveCount", pendingLeaveCount);
        result.put("lowAttendanceStudents", lowAttendanceStudents);
        return result;
    }

    public Map<String, Object> getCreditRanking(Long classId) {
        Map<String, Object> result = new LinkedHashMap<>();
        LambdaQueryWrapper<Student> studentW = new LambdaQueryWrapper<>();
        if (classId != null) studentW.eq(Student::getClassId, classId);
        studentW.isNotNull(Student::getCreditScore);
        studentW.orderByDesc(Student::getCreditScore);
        List<Student> students = studentMapper.selectList(studentW);

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Student s : students) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("studentId", s.getId());
            item.put("studentName", s.getName());
            item.put("studentNo", s.getStudentNo());
            item.put("creditScore", s.getCreditScore() != null ? s.getCreditScore() : 100);
            item.put("creditEarned", s.getCreditEarned() != null ? s.getCreditEarned() : 0);
            item.put("creditDeducted", s.getCreditDeducted() != null ? s.getCreditDeducted() : 0);
            if (s.getClassId() != null) {
                var ci = classMapper.selectById(s.getClassId());
                item.put("className", ci != null ? ci.getClassName() : "");
            }
            String grade = "C";
            int score = s.getCreditScore() != null ? s.getCreditScore() : 100;
            if (score >= 180) grade = "S";
            else if (score >= 150) grade = "A";
            else if (score >= 120) grade = "B";
            else if (score >= 80) grade = "C";
            else grade = "D";
            item.put("grade", grade);
            ranking.add(item);
        }
        result.put("ranking", ranking);
        result.put("total", ranking.size());
        return result;
    }

    private String generateSuggestion(double attendanceRate, long handRaiseCount, long violationCount, long presentCount, long absentCount) {
        List<String> s = new ArrayList<>();
        if (attendanceRate >= 95) s.add("本周出勤率" + attendanceRate + "%，全勤表现优异，继续保持！");
        else if (attendanceRate >= 80) s.add("本周出勤率" + attendanceRate + "%，整体不错，争取下周全勤。");
        else s.add("本周出勤率" + attendanceRate + "%，缺勤" + absentCount + "次，请注意按时上课。");

        if (handRaiseCount >= 5) s.add("本周你举手" + handRaiseCount + "次，课堂参与度很高，继续保持主动学习的态度！");
        else if (handRaiseCount >= 3) s.add("本周你举手" + handRaiseCount + "次，表现不错，试着更多参与课堂互动。");
        else if (handRaiseCount > 0) s.add("本周你举手" + handRaiseCount + "次，建议多主动回答问题，提升课堂参与感。");
        else s.add("本周未记录到举手发言，建议积极参与课堂讨论，勇于表达自己的观点。");

        if (violationCount > 10) s.add("本周违规行为记录" + violationCount + "次，较多，建议减少使用手机，保持专注。");
        else if (violationCount > 5) s.add("本周有" + violationCount + "次课堂注意力分散记录，可以尝试调整坐姿，提高专注力。");
        else if (violationCount > 0) s.add("本周仅有" + violationCount + "次轻度注意力分散，整体课堂表现良好。");
        else s.add("本周未检测到违规行为，课堂自律性很棒！");

        if (handRaiseCount >= 3 && violationCount <= 5 && attendanceRate >= 90) s.add("综合评价：本周表现优秀，是同学们的榜样，请继续坚持！");
        else if (handRaiseCount >= 1 && violationCount <= 10 && attendanceRate >= 80) s.add("综合评价：本周表现良好，希望下周更进一步！");
        else s.add("综合评价：改进空间较大，建议制定学习计划，逐步提升课堂表现。");

        return String.join(" ", s);
    }
}
