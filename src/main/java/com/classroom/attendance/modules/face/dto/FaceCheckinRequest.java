package com.classroom.attendance.modules.face.dto;

import lombok.Data;

@Data
public class FaceCheckinRequest {
    private Long courseId;
    private String image;
}
