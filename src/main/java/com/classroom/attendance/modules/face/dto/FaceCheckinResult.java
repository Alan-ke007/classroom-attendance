package com.classroom.attendance.modules.face.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceCheckinResult {
    private String faceStatus;        // VERIFIED / NEED_REVIEW / REJECTED
    private BigDecimal confidence;    // 余弦相似度（复用 attendance.confidence）
    private String status;            // PRESENT / LATE / ABSENT（写库后考勤状态）
    private LocalDateTime checkInTime;
    private String message;
    private String reason;            // 可选：NO_ENROLLMENT / MISMATCH / ALGO_UNAVAILABLE / MOCK
}
