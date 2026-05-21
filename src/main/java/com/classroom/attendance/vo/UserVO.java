package com.classroom.attendance.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String role;
    private String email;
    private String phone;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
