package com.classroom.attendance.modules.student.service;

import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final StudentMapper studentMapper;

    private static final int SCORE_MIN = 0;
    private static final int SCORE_MAX = 200;
    private static final int SCORE_INITIAL = 100;

    /** 出勤 +2 */
    public void addAttendancePresent(Long studentId) {
        addScore(studentId, +2, "出勤签到");
    }

    /** 迟到 -1 */
    public void addAttendanceLate(Long studentId) {
        addScore(studentId, -1, "迟到签到");
    }

    /** 缺勤 -3 */
    public void addAttendanceAbsent(Long studentId) {
        addScore(studentId, -3, "缺勤");
    }

    /** 行为加分：举手/阅读/写作 +1 */
    public void addPositiveBehavior(Long studentId) {
        addScore(studentId, +1, "积极课堂行为");
    }

    /** 行为扣分：玩手机 -2 */
    public void addPhoneViolation(Long studentId) {
        addScore(studentId, -2, "课堂使用手机");
    }

    /** 行为扣分：低头/侧身/睡觉 -1 */
    public void addMinorViolation(Long studentId) {
        addScore(studentId, -1, "课堂注意力分散");
    }

    public int getCreditScore(Long studentId) {
        Student s = studentMapper.selectById(studentId);
        return s != null && s.getCreditScore() != null ? s.getCreditScore() : SCORE_INITIAL;
    }

    private void addScore(Long studentId, int delta, String reason) {
        if (studentId == null) return;
        Student s = studentMapper.selectById(studentId);
        if (s == null) return;
        int current = s.getCreditScore() != null ? s.getCreditScore() : SCORE_INITIAL;
        int newScore = clamp(current + delta);

        s.setCreditScore(newScore);
        if (delta > 0) {
            s.setCreditEarned((s.getCreditEarned() != null ? s.getCreditEarned() : 0) + delta);
        } else if (delta < 0) {
            s.setCreditDeducted((s.getCreditDeducted() != null ? s.getCreditDeducted() : 0) + Math.abs(delta));
        }
        studentMapper.updateById(s);
        log.debug("学生 {} 学风分变化: {} → {} ({}), 原因: {}", studentId, current, newScore, delta > 0 ? "+" + delta : "" + delta, reason);
    }

    private int clamp(int score) {
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, score));
    }
}
