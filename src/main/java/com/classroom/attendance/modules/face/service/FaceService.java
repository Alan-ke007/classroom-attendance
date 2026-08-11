package com.classroom.attendance.modules.face.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classroom.attendance.config.AlgorithmClient;
import com.classroom.attendance.infrastructure.exception.AlgoUnavailableException;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.enums.AttendanceStatus;
import com.classroom.attendance.modules.attendance.enums.FaceStatus;
import com.classroom.attendance.modules.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.face.dto.EnrollResult;
import com.classroom.attendance.modules.face.dto.FaceCheckinResult;
import com.classroom.attendance.modules.face.dto.RecognizeResult;
import com.classroom.attendance.modules.face.entity.FaceEmbedding;
import com.classroom.attendance.modules.face.mapper.FaceEmbeddingMapper;
import com.classroom.attendance.modules.student.service.CreditScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 人脸业务：建档（enroll）、核验代理（verify）、合并端点（faceCheckin）。
 *
 * <p>信任边界（R6）：最终判定权在后端。本服务持 face_embedding gallery，调算法仅做「提特征」，
 * 1:1 余弦比对与 VERIFIED 判定均在应用层完成；小程序仅透传图片，不得自行断言 VERIFIED。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FaceService {

    private static final int DIM = AlgorithmClient.EMBEDDING_DIM;

    private final FaceEmbeddingMapper faceEmbeddingMapper;
    private final AttendanceMapper attendanceMapper;
    private final CourseMapper courseMapper;
    private final CreditScoreService creditScoreService;
    private final AlgorithmClient algorithmClient;

    // ===================== 建档 F3/F4/F6 =====================

    public EnrollResult enroll(List<String> images, Long requestedStudentId) {
        Long studentId = SecurityUtil.getCurrentStudentId();
        BusinessException.notNull(studentId, "未获取到学生身份，请先登录");

        if (requestedStudentId != null && !requestedStudentId.equals(studentId)) {
            throw new BusinessException(403, "越权操作：studentId 与登录身份不一致");
        }
        BusinessException.isTrue(images != null && !images.isEmpty() && images.size() <= 3,
                40001, "建档图片数量须为 1~3 张");

        List<float[]> vectors = new ArrayList<>();
        for (String img : images) {
            BusinessException.isTrue(img != null && !img.isBlank(), 40004, "图片数据不能为空");
            // 逐张调 extract；任一图非单人脸(40002/40003)或解码失败(40004) → 抛异常，未落库即回滚
            vectors.add(algorithmClient.extract(img).getEmbedding());
        }

        float[] aggregated = aggregate(vectors);
        byte[] blob = floatsToBytes(aggregated);

        FaceEmbedding embedding = FaceEmbedding.builder()
                .studentId(studentId)
                .embedding(blob)
                .faceCount(images.size())
                .source(algorithmClient.isMock() ? "mock" : "enroll")
                .modelVersion(algorithmClient.getModel())
                .imageRef(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(0)
                .build();
        upsert(embedding);

        return EnrollResult.builder()
                .studentId(studentId)
                .enrolledAt(embedding.getUpdatedAt())
                .faceCount(images.size())
                .source(embedding.getSource())
                .modelVersion(embedding.getModelVersion())
                .build();
    }

    /** 幂等 upsert（覆盖式重建档，F3）。 */
    private void upsert(FaceEmbedding embedding) {
        FaceEmbedding existing = faceEmbeddingMapper.selectById(embedding.getStudentId());
        if (existing == null) {
            faceEmbeddingMapper.insert(embedding);
        } else {
            embedding.setCreatedAt(existing.getCreatedAt()); // 保留首次建档时间
            embedding.setUpdatedAt(LocalDateTime.now());
            faceEmbeddingMapper.updateById(embedding);
        }
    }

    // ===================== 核验代理 F5 =====================

    public RecognizeResult verify(String image, Long requestedStudentId) {
        Long studentId = SecurityUtil.getCurrentStudentId();
        BusinessException.notNull(studentId, "未获取到学生身份，请先登录");

        FaceEmbedding rec = faceEmbeddingMapper.selectById(studentId);
        if (rec == null) {
            return RecognizeResult.builder()
                    .matched(false)
                    .confidence(BigDecimal.ZERO)
                    .studentId(studentId)
                    .reason("NO_ENROLLMENT")
                    .build();
        }

        // 算法不可达 → AlgoUnavailableException(40005) 向上抛出，由全局异常映射
        float[] current = algorithmClient.extract(image).getEmbedding();
        float[] stored = bytesToFloats(rec.getEmbedding(), DIM);
        double sim = cosine(current, stored);
        boolean matched = sim >= algorithmClient.getThreshold();

        return RecognizeResult.builder()
                .matched(matched)
                .confidence(BigDecimal.valueOf(sim))
                .studentId(studentId)
                .reason(matched ? null : "MISMATCH")
                .build();
    }

    // ===================== 合并端点：提取+比对+写签到 F7/F8 =====================

    @Transactional
    public FaceCheckinResult faceCheckin(Long courseId, String image) {
        Long studentId = SecurityUtil.getCurrentStudentId();
        BusinessException.notNull(studentId, "未获取到学生身份，请先登录");
        BusinessException.notNull(courseId, "课程ID不能为空");

        Course course = courseMapper.selectById(courseId);
        BusinessException.notNull(course, "课程不存在");

        LocalDate today = LocalDate.now();

        // 同课程同日不重复签到（仿 QRCodeController，避免降级放行产生重复签到）
        Attendance existing = attendanceMapper.selectOne(new LambdaQueryWrapper<Attendance>()
                .eq(Attendance::getStudentId, studentId)
                .eq(Attendance::getCourseId, courseId)
                .eq(Attendance::getAttendanceDate, today));
        BusinessException.isTrue(existing == null, "今日已签到，请勿重复签到");

        FaceEmbedding rec = faceEmbeddingMapper.selectById(studentId);
        if (rec == null) {
            // 未建档：不写签到，引导建档
            return FaceCheckinResult.builder()
                    .faceStatus(FaceStatus.REJECTED.name())
                    .confidence(null)
                    .status(null)
                    .checkInTime(null)
                    .message("请先完成人脸建档后再签到")
                    .reason("NO_ENROLLMENT")
                    .build();
        }

        float[] current;
        try {
            current = algorithmClient.extract(image).getEmbedding();
        } catch (AlgoUnavailableException e) {
            // 算法不可达 → 降级：仍写签到，标记待复核（F8/NF4）
            return degradeCheckin(studentId, course, "已签到，人脸待复核", "ALGO_UNAVAILABLE");
        }

        float[] stored = bytesToFloats(rec.getEmbedding(), DIM);
        double sim = cosine(current, stored);
        boolean matched = sim >= algorithmClient.getThreshold();

        if (algorithmClient.isMock()) {
            // F10：mock 不写 VERIFIED，降级为待复核（不静默放行真实核验）
            return degradeCheckin(studentId, course, "已签到（mock 联调），人脸待复核", "MOCK");
        }

        if (matched) {
            AttendanceStatus status = determineStatus(course);
            Attendance attendance = buildAttendance(studentId, course, status,
                    FaceStatus.VERIFIED, BigDecimal.valueOf(sim), "miniapp_checkin");
            attendanceMapper.insert(attendance);
            applyCredit(studentId, status);
            return FaceCheckinResult.builder()
                    .faceStatus(FaceStatus.VERIFIED.name())
                    .confidence(BigDecimal.valueOf(sim))
                    .status(status.name())
                    .checkInTime(attendance.getCheckInTime())
                    .message("签到成功")
                    .build();
        }

        // 比对不通过（算法可达）：不写签到，提示不通过
        return FaceCheckinResult.builder()
                .faceStatus(FaceStatus.REJECTED.name())
                .confidence(BigDecimal.valueOf(sim))
                .status(null)
                .checkInTime(null)
                .message("人脸核验未通过")
                .reason("MISMATCH")
                .build();
    }

    private FaceCheckinResult degradeCheckin(Long studentId, Course course, String message, String reason) {
        AttendanceStatus status = determineStatus(course);
        Attendance attendance = buildAttendance(studentId, course, status,
                FaceStatus.NEED_REVIEW, null, "miniapp_checkin");
        attendanceMapper.insert(attendance);
        applyCredit(studentId, status);
        return FaceCheckinResult.builder()
                .faceStatus(FaceStatus.NEED_REVIEW.name())
                .confidence(null)
                .status(status.name())
                .checkInTime(attendance.getCheckInTime())
                .message(message)
                .reason(reason)
                .build();
    }

    // ===================== 工具：状态/学分/编解码/余弦 =====================

    private Attendance buildAttendance(Long studentId, Course course, AttendanceStatus status,
                                        FaceStatus faceStatus, BigDecimal confidence, String source) {
        LocalDateTime now = LocalDateTime.now();
        return Attendance.builder()
                .studentId(studentId)
                .courseId(course.getId())
                .classId(course.getClassId())
                .attendanceDate(LocalDate.now())
                .status(status)
                .checkInTime(now)
                .confidence(confidence)
                .faceStatus(faceStatus.name())
                .faceSource(source)
                .remark(status == AttendanceStatus.PRESENT ? "人脸核验签到" : "人脸核验·" + status.getDescription())
                .build();
    }

    /** 与 QRCodeController.determineStatus 保持一致：上课 10min 内 PRESENT，30min 内 LATE，其余 ABSENT。 */
    private AttendanceStatus determineStatus(Course course) {
        LocalTime now = LocalTime.now();
        LocalTime start = course.getStartTime();
        long minutesAfterStart = now.toSecondOfDay() - start.toSecondOfDay();
        minutesAfterStart = minutesAfterStart / 60;
        if (minutesAfterStart <= 10) return AttendanceStatus.PRESENT;
        if (minutesAfterStart <= 30) return AttendanceStatus.LATE;
        return AttendanceStatus.ABSENT;
    }

    private void applyCredit(Long studentId, AttendanceStatus status) {
        if (status == AttendanceStatus.PRESENT) creditScoreService.addAttendancePresent(studentId);
        else if (status == AttendanceStatus.LATE) creditScoreService.addAttendanceLate(studentId);
        else if (status == AttendanceStatus.ABSENT) creditScoreService.addAttendanceAbsent(studentId);
    }

    /** 多张特征平均后 L2 归一化，得到 1 条聚合特征。 */
    private float[] aggregate(List<float[]> vectors) {
        float[] acc = new float[DIM];
        for (float[] v : vectors) {
            for (int i = 0; i < DIM; i++) acc[i] += v[i];
        }
        for (int i = 0; i < DIM; i++) acc[i] /= vectors.size();
        return normalize(acc);
    }

    private float[] normalize(float[] v) {
        double norm = 0.0;
        for (float x : v) norm += (double) x * x;
        norm = Math.sqrt(norm);
        if (norm == 0.0) return v;
        float[] out = new float[DIM];
        for (int i = 0; i < DIM; i++) out[i] = (float) (v[i] / norm);
        return out;
    }

    private byte[] floatsToBytes(float[] v) {
        ByteBuffer bb = ByteBuffer.allocate(DIM * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (float x : v) bb.putFloat(x);
        return bb.array();
    }

    private float[] bytesToFloats(byte[] b, int n) {
        float[] out = new float[n];
        ByteBuffer bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < n; i++) out[i] = bb.getFloat();
        return out;
    }

    /** 余弦相似度（向量已 L2 归一化时等价于点积）。 */
    private double cosine(float[] a, float[] b) {
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < DIM; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        if (na == 0.0 || nb == 0.0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
