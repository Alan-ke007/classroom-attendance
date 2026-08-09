package com.classroom.attendance.modules.face.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollResult {
    private Long studentId;
    private LocalDateTime enrolledAt;
    private Integer faceCount;
    private String source;
    private String modelVersion;
}
