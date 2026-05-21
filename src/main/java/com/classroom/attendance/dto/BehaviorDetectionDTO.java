package com.classroom.attendance.dto;

import lombok.Data;

/**
 * AI 行为检测结果 DTO
 */
@Data
public class BehaviorDetectionDTO {
    private String behaviorType;
    private Double confidence;
    private Long classId;
    private Long courseId;
}
