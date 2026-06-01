package com.classroom.attendance.modules.behavior.dto;

import lombok.Data;

@Data
public class BehaviorDetectionDTO {
    private String behaviorType;
    private Double confidence;
    private Long classId;
    private Long courseId;
    private Long studentId;
}
