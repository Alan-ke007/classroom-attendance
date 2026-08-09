package com.classroom.attendance.modules.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String otherUserRole;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
