package com.classroom.attendance.modules.face.dto;

import lombok.Data;

import java.util.List;

@Data
public class EnrollRequest {
    private List<String> images;
    private Long studentId; // 可选；服务端忽略或一致性校验，真正身份取自 SecurityUtil
}
