package com.classroom.attendance.dto;

import lombok.Data;

@Data
public class SendMessageDTO {
    private Long receiverId;
    private String content;
}
