package com.classroom.attendance.modules.face.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecognizeResult {
    private boolean matched;
    private BigDecimal confidence;
    private Long studentId;
    private String reason; // null / NO_ENROLLMENT / MISMATCH
}
