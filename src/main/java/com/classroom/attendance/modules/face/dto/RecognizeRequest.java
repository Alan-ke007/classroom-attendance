package com.classroom.attendance.modules.face.dto;

import lombok.Data;

@Data
public class RecognizeRequest {
    private String image;
    private Long studentId; // 可选；服务端忽略或一致性校验
}
