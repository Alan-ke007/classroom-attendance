package com.classroom.attendance.modules.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("password_reset_token")
public class PasswordResetToken implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;
    private String tokenHash;   // 原始令牌的 SHA-256（服务端只存哈希，原始令牌不下发）
    private LocalDateTime expiry;
    private Integer used;        // 0=未用 1=已用（一次性）
    private LocalDateTime createTime;
}
