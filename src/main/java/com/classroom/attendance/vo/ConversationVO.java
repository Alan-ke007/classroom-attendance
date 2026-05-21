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
public class ConversationVO {
    private Long otherUserId;
    private String otherUserName;
    private String otherUserAvatar;
    private String otherUserRole;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
}
