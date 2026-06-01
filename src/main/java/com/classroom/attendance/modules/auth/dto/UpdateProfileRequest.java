package com.classroom.attendance.modules.auth.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String realName;
    private String email;
    private String phone;
}
