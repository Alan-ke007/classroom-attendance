package com.classroom.attendance.modules.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classroom.attendance.modules.behavior.dto.AttentionResult;
import com.classroom.attendance.modules.behavior.entity.BehaviorRecord;
import com.classroom.attendance.modules.behavior.mapper.BehaviorRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 课堂专注度评分服务（智能考勤核心算法）。
 *
 * <p>将 YOLOv8 课堂行为识别结果量化为“考勤质量/专注度”信号：传统考勤只判断“人在不在”，
 * 本服务进一步用行为识别判断“人是否在听课”，从而把考勤从“出勤”升级为“出勤质量”。
 * 算法与公式说明见 {@link AttentionResult}。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttentionService {

    private final BehaviorRecordMapper behaviorRecordMapper;

    /** 专注行为：举手、阅读、书写 */
    private static final Set<String> FOCUS_TYPES = Set.of("raising_hand", "reading", "writing");
    /** 违纪行为：玩手机、低头、趴桌 */
    private static final Set<String> VIOLATION_TYPES = Set.of("using_phone", "bowing_head", "leaning_over");
    /** 违纪权重 α：违纪一次相当于抵消 1.2 次专注行为 */
    private static final double ALPHA = 1.2;
    /** 平滑项 ε，避免分母为 0 */
    private static final double EPS = 1e-6;

    /** 学生某课程（或全部）在指定时间窗内的专注度。 */
    public AttentionResult computeStudentAttention(Long studentId, Long courseId,
                                                   LocalDateTime start, LocalDateTime end) {
        return score(query(studentId, courseId, start, end));
    }

    /** 班级整体专注度（聚合全班样本算一个 ATI）。 */
    public AttentionResult computeClassAttention(Long courseId, LocalDateTime start, LocalDateTime end) {
        return score(query(null, courseId, start, end));
    }

    /** 班级逐学生专注度明细（报表 / 论文用）。 */
    public List<AttentionResult> computeClassAttentionDetail(Long courseId, LocalDateTime start, LocalDateTime end) {
        List<BehaviorRecord> records = query(null, courseId, start, end);
        Map<Long, List<BehaviorRecord>> byStudent = records.stream()
                .filter(r -> r.getStudentId() != null)
                .collect(Collectors.groupingBy(BehaviorRecord::getStudentId));
        return byStudent.values().stream()
                .map(this::score)
                .collect(Collectors.toList());
    }

    private List<BehaviorRecord> query(Long studentId, Long courseId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<BehaviorRecord> w = new LambdaQueryWrapper<>();
        if (studentId != null) w.eq(BehaviorRecord::getStudentId, studentId);
        if (courseId != null) w.eq(BehaviorRecord::getCourseId, courseId);
        if (start != null) w.ge(BehaviorRecord::getBehaviorTime, start);
        if (end != null) w.le(BehaviorRecord::getBehaviorTime, end);
        return behaviorRecordMapper.selectList(w);
    }

    private AttentionResult score(List<BehaviorRecord> records) {
        long nf = records.stream().filter(r -> FOCUS_TYPES.contains(r.getBehaviorType())).count();
        long nv = records.stream().filter(r -> VIOLATION_TYPES.contains(r.getBehaviorType())).count();
        int total = records.size();
        double ati = (nf - ALPHA * nv) / (nf + nv + EPS) * 100.0;
        ati = Math.max(-100.0, Math.min(100.0, ati));
        return AttentionResult.builder()
                .totalSamples(total)
                .focusCount((int) nf)
                .violationCount((int) nv)
                .ati(ati)
                .level(levelOf(ati))
                .build();
    }

    private String levelOf(double ati) {
        if (ati >= 60) return "优";
        if (ati >= 40) return "良";
        if (ati >= 20) return "中";
        return "差";
    }
}
