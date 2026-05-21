package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classroom.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.mapper.BehaviorRecordMapper;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.model.Attendance;
import com.classroom.attendance.model.BehaviorRecord;
import com.classroom.attendance.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Autowired
    private BehaviorRecordMapper behaviorRecordMapper;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long studentCount = studentMapper.selectCount(null);
        long courseCount = courseMapper.selectCount(null);
        long totalAttendance = attendanceMapper.selectCount(null);

        LocalDate today = LocalDate.now();
        long todayAttendance = attendanceMapper.selectCount(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getAttendanceDate, today)
        );

        long presentCount = attendanceMapper.selectCount(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStatus, "present")
        );

        long unhandledBehavior = behaviorRecordMapper.selectCount(
                new LambdaQueryWrapper<BehaviorRecord>()
                        .eq(BehaviorRecord::getHandled, 0)
        );

        stats.put("studentCount", studentCount);
        stats.put("courseCount", courseCount);
        stats.put("totalAttendance", totalAttendance);
        stats.put("todayAttendance", todayAttendance);
        stats.put("presentCount", presentCount);
        stats.put("unhandledBehavior", unhandledBehavior);

        if (totalAttendance > 0) {
            double rate = BigDecimal.valueOf(presentCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAttendance), 1, RoundingMode.HALF_UP)
                    .doubleValue();
            stats.put("attendanceRate", rate);
        } else {
            stats.put("attendanceRate", 100.0);
        }

        return stats;
    }

    @Override
    public Map<String, Object> getAttendanceStats(LocalDate startDate, LocalDate endDate, Long filterStudentId, Collection<Long> filterClassIds) {
        Map<String, Object> result = new LinkedHashMap<>();

        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(Attendance::getAttendanceDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Attendance::getAttendanceDate, endDate);
        }
        if (filterStudentId != null) {
            wrapper.eq(Attendance::getStudentId, filterStudentId);
        }
        if (!CollectionUtils.isEmpty(filterClassIds)) {
            wrapper.in(Attendance::getClassId, filterClassIds);
        }

        List<Attendance> all = attendanceMapper.selectList(wrapper);

        long present = all.stream().filter(a -> "present".equals(a.getStatus())).count();
        long late = all.stream().filter(a -> "late".equals(a.getStatus())).count();
        long absent = all.stream().filter(a -> "absent".equals(a.getStatus())).count();
        long leave = all.stream().filter(a -> "leave".equals(a.getStatus())).count();

        result.put("total", all.size());
        result.put("present", present);
        result.put("late", late);
        result.put("absent", absent);
        result.put("leave", leave);

        // 按日期分组
        Map<LocalDate, Long> dailyCount = all.stream()
                .collect(Collectors.groupingBy(Attendance::getAttendanceDate, TreeMap::new, Collectors.counting()));
        result.put("daily", dailyCount);

        return result;
    }

    @Override
    public Map<String, Object> getStudentAttendanceRanking(Long classId) {
        // 按学生ID分组统计出勤率
        Map<String, Object> result = new LinkedHashMap<>();

        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        if (classId != null) {
            wrapper.eq(Attendance::getClassId, classId);
        }

        List<Attendance> list = attendanceMapper.selectList(wrapper);

        Map<Long, List<Attendance>> grouped = list.stream()
                .collect(Collectors.groupingBy(Attendance::getStudentId));

        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Map.Entry<Long, List<Attendance>> entry : grouped.entrySet()) {
            long total = entry.getValue().size();
            long present = entry.getValue().stream()
                    .filter(a -> "present".equals(a.getStatus()))
                    .count();
            double rate = total > 0
                    ? BigDecimal.valueOf(present).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP)
                    .doubleValue()
                    : 0;

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

    @Override
    public Map<String, Object> getStudentDashboardStats(Long studentId) {
        Map<String, Object> stats = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        java.time.DayOfWeek dayOfWeek = today.getDayOfWeek();
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        String todayWeekDay = weekDays[dayOfWeek.getValue() % 7];

        // 获取该学生信息
        var student = studentMapper.selectById(studentId);
        if (student == null) {
            stats.put("totalCourses", 0);
            stats.put("presentCount", 0);
            stats.put("absentCount", 0);
            stats.put("todayCourses", 0);
            stats.put("attendanceRate", 100.0);
            return stats;
        }

        // 该学生班级的所有课程
        long totalCourses = courseMapper.selectCount(
                new LambdaQueryWrapper<com.classroom.attendance.model.Course>()
                        .eq(com.classroom.attendance.model.Course::getClassId, student.getClassId())
        );

        // 该学生今日课程数
        long todayCourses = courseMapper.selectCount(
                new LambdaQueryWrapper<com.classroom.attendance.model.Course>()
                        .eq(com.classroom.attendance.model.Course::getClassId, student.getClassId())
                        .eq(com.classroom.attendance.model.Course::getWeekDay, todayWeekDay)
        );

        // 该学生的所有考勤记录
        List<Attendance> studentAttendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStudentId, studentId)
        );

        long presentCount = studentAttendances.stream()
                .filter(a -> "present".equals(a.getStatus())).count();
        long absentCount = studentAttendances.stream()
                .filter(a -> "absent".equals(a.getStatus())).count();
        long totalAttendance = studentAttendances.size();

        double rate = 100.0;
        if (totalAttendance > 0) {
            rate = BigDecimal.valueOf(presentCount + studentAttendances.stream()
                            .filter(a -> "late".equals(a.getStatus())).count())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAttendance), 1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        stats.put("totalCourses", totalCourses);
        stats.put("todayCourses", todayCourses);
        stats.put("presentCount", presentCount);
        stats.put("absentCount", absentCount);
        stats.put("totalAttendance", totalAttendance);
        stats.put("attendanceRate", rate);
        stats.put("studentName", student.getName());
        stats.put("studentNo", student.getStudentNo());

        return stats;
    }

    @Override
    public Map<String, Object> getWeeklyReport(Long studentId) {
        Map<String, Object> report = new LinkedHashMap<>();

        // 计算本周一的日期
        LocalDate today = LocalDate.now();
        LocalDate monday = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate sunday = monday.plusDays(6);

        report.put("weekStart", monday.toString());
        report.put("weekEnd", sunday.toString());

        var student = studentMapper.selectById(studentId);
        if (student == null) {
            report.put("attendanceRate", 0);
            report.put("totalClasses", 0);
            report.put("presentCount", 0);
            report.put("handRaiseCount", 0);
            report.put("violationCount", 0);
            report.put("violationDetails", Map.of());
            report.put("trend", List.of());
            report.put("suggestion", "暂无数据，请先完成课程签到");
            return report;
        }

        // 本周考勤统计
        List<Attendance> weekAttendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .eq(Attendance::getStudentId, studentId)
                        .ge(Attendance::getAttendanceDate, monday)
                        .le(Attendance::getAttendanceDate, sunday)
        );

        int total = weekAttendances.size();
        long present = weekAttendances.stream().filter(a -> "present".equals(a.getStatus())).count();
        long late = weekAttendances.stream().filter(a -> "late".equals(a.getStatus())).count();
        long absent = weekAttendances.stream().filter(a -> "absent".equals(a.getStatus())).count();

        double attendanceRate = total > 0
                ? BigDecimal.valueOf(present + late).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP).doubleValue()
                : 100.0;

        report.put("totalClasses", total);
        report.put("presentCount", present);
        report.put("lateCount", late);
        report.put("absentCount", absent);
        report.put("attendanceRate", attendanceRate);

        // 本周行为统计（所有行为类型分组统计）
        List<BehaviorRecord> weekBehaviors = behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>()
                        .eq(BehaviorRecord::getStudentId, studentId)
                        .ge(BehaviorRecord::getBehaviorTime, monday.atStartOfDay())
                        .le(BehaviorRecord::getBehaviorTime, sunday.atTime(23, 59, 59))
        );

        // 按行为类型分组计数
        Map<String, Long> behaviorCounts = weekBehaviors.stream()
                .collect(Collectors.groupingBy(BehaviorRecord::getBehaviorType, Collectors.counting()));

        long handRaiseCount = behaviorCounts.getOrDefault("raising_hand", 0L);

        // 违规行为：玩手机、低头、趴桌
        Map<String, Long> violations = new LinkedHashMap<>();
        violations.put("using_phone", behaviorCounts.getOrDefault("using_phone", 0L));
        violations.put("bowing_head", behaviorCounts.getOrDefault("bowing_head", 0L));
        violations.put("leaning_over", behaviorCounts.getOrDefault("leaning_over", 0L));
        long violationCount = violations.values().stream().mapToLong(Long::longValue).sum();

        report.put("handRaiseCount", handRaiseCount);
        report.put("violationCount", violationCount);
        report.put("violationDetails", violations);

        // 按天统计举手趋势（本周每天）
        Map<LocalDate, Long> dailyHandRaise = weekBehaviors.stream()
                .filter(b -> "raising_hand".equals(b.getBehaviorType()))
                .collect(Collectors.groupingBy(
                        b -> b.getBehaviorTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        List<Map<String, Object>> trend = new ArrayList<>();
        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", d.toString());
            point.put("count", dailyHandRaise.getOrDefault(d, 0L).intValue());
            trend.add(point);
        }
        report.put("trend", trend);

        // 生成AI学习建议
        report.put("suggestion", generateSuggestion(attendanceRate, handRaiseCount, violationCount, present, absent));

        return report;
    }

    @Override
    public Map<String, Object> getClassQuality() {
        Map<String, Object> quality = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);

        // 本周所有考勤记录
        List<Attendance> weekAttendances = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>()
                        .ge(Attendance::getAttendanceDate, weekStart)
                        .le(Attendance::getAttendanceDate, today)
        );

        int totalAttendance = weekAttendances.size();
        long presentCount = weekAttendances.stream().filter(a -> "present".equals(a.getStatus())).count();
        long lateCount = weekAttendances.stream().filter(a -> "late".equals(a.getStatus())).count();
        long absentCount = weekAttendances.stream().filter(a -> "absent".equals(a.getStatus())).count();

        double attendanceRate = totalAttendance > 0
                ? BigDecimal.valueOf(presentCount + lateCount).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalAttendance), 1, RoundingMode.HALF_UP).doubleValue()
                : 100.0;

        // 本周所有行为记录
        List<BehaviorRecord> weekBehaviors = behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>()
                        .ge(BehaviorRecord::getBehaviorTime, weekStart.atStartOfDay())
                        .le(BehaviorRecord::getBehaviorTime, today.atTime(23, 59, 59))
        );

        long totalBehaviors = weekBehaviors.size();

        // 积极行为（举手、阅读、写作）
        long positiveBehaviors = weekBehaviors.stream()
                .filter(b -> List.of("raising_hand", "reading", "writing").contains(b.getBehaviorType()))
                .count();

        // 违规行为（玩手机、低头、趴桌）
        long violationBehaviors = weekBehaviors.stream()
                .filter(b -> List.of("using_phone", "bowing_head", "leaning_over").contains(b.getBehaviorType()))
                .count();

        // 计算各维度分数（0-100）
        double attendanceScore = attendanceRate; // 出勤率本身就是0-100

        double handRaiseRate = totalBehaviors > 0
                ? (double) weekBehaviors.stream().filter(b -> "raising_hand".equals(b.getBehaviorType())).count()
                    / totalBehaviors * 100
                : 0;
        double handRaiseScore = Math.min(handRaiseRate * 5, 100); // 放大举手率，上限100

        double violationRate = totalBehaviors > 0
                ? (double) violationBehaviors / totalBehaviors * 100
                : 0;
        double violationScore = Math.max(100 - violationRate * 5, 0); // 违规越少分越高

        // 专注度趋势：积极行为占总行为比例
        double focusRate = totalBehaviors > 0
                ? (double) positiveBehaviors / totalBehaviors * 100
                : 100;
        double focusScore = focusRate;

        // 综合评分 = 出勤率30% + 举手率25% + 违规反向25% + 专注度20%
        double overallScore = attendanceScore * 0.30 + handRaiseScore * 0.25
                + violationScore * 0.25 + focusScore * 0.20;
        overallScore = BigDecimal.valueOf(overallScore)
                .setScale(1, RoundingMode.HALF_UP).doubleValue();

        quality.put("overallScore", overallScore);
        quality.put("attendanceScore", BigDecimal.valueOf(attendanceScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        quality.put("handRaiseScore", BigDecimal.valueOf(handRaiseScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        quality.put("violationScore", BigDecimal.valueOf(violationScore).setScale(1, RoundingMode.HALF_UP).doubleValue());
        quality.put("focusScore", BigDecimal.valueOf(focusScore).setScale(1, RoundingMode.HALF_UP).doubleValue());

        quality.put("totalAttendance", totalAttendance);
        quality.put("presentCount", presentCount);
        quality.put("lateCount", lateCount);
        quality.put("absentCount", absentCount);
        quality.put("attendanceRate", attendanceRate);

        quality.put("totalBehaviors", totalBehaviors);
        quality.put("positiveBehaviors", positiveBehaviors);
        quality.put("violationBehaviors", violationBehaviors);

        quality.put("weightAttendance", "30%");
        quality.put("weightHandRaise", "25%");
        quality.put("weightViolation", "25%");
        quality.put("weightFocus", "20%");

        return quality;
    }

    /**
     * 根据统计数据生成AI学习建议
     */
    private String generateSuggestion(double attendanceRate, long handRaiseCount, long violationCount, long presentCount, long absentCount) {
        List<String> suggestions = new ArrayList<>();

        if (attendanceRate >= 95) {
            suggestions.add("本周出勤率" + attendanceRate + "%，全勤表现优异，继续保持！");
        } else if (attendanceRate >= 80) {
            suggestions.add("本周出勤率" + attendanceRate + "%，整体不错，争取下周全勤。");
        } else {
            suggestions.add("本周出勤率" + attendanceRate + "%，缺勤" + absentCount + "次，请注意按时上课。");
        }

        if (handRaiseCount >= 5) {
            suggestions.add("本周你举手" + handRaiseCount + "次，课堂参与度很高，继续保持主动学习的态度！");
        } else if (handRaiseCount >= 3) {
            suggestions.add("本周你举手" + handRaiseCount + "次，表现不错，试着更多参与课堂互动。");
        } else if (handRaiseCount > 0) {
            suggestions.add("本周你举手" + handRaiseCount + "次，建议多主动回答问题，提升课堂参与感。");
        } else {
            suggestions.add("本周未记录到举手发言，建议积极参与课堂讨论，勇于表达自己的观点。");
        }

        if (violationCount > 10) {
            suggestions.add("本周违规行为记录" + violationCount + "次，较多，建议减少使用手机，保持专注。");
        } else if (violationCount > 5) {
            suggestions.add("本周有" + violationCount + "次课堂注意力分散记录，可以尝试调整坐姿，提高专注力。");
        } else if (violationCount > 0) {
            suggestions.add("本周仅有" + violationCount + "次轻度注意力分散，整体课堂表现良好。");
        } else {
            suggestions.add("本周未检测到违规行为，课堂自律性很棒！");
        }

        if (handRaiseCount >= 3 && violationCount <= 5 && attendanceRate >= 90) {
            suggestions.add("综合评价：本周表现优秀，是同学们的榜样，请继续坚持！");
        } else if (handRaiseCount >= 1 && violationCount <= 10 && attendanceRate >= 80) {
            suggestions.add("综合评价：本周表现良好，希望下周更进一步！");
        } else {
            suggestions.add("综合评价：改进空间较大，建议制定学习计划，逐步提升课堂表现。");
        }

        return String.join(" ", suggestions);
    }
}
