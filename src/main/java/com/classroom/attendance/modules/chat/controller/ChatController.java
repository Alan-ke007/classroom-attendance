package com.classroom.attendance.modules.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.auth.entity.User;
import com.classroom.attendance.modules.chat.dto.ConversationResponse;
import com.classroom.attendance.modules.chat.dto.SendMessageRequest;
import com.classroom.attendance.modules.chat.entity.ChatMessage;
import com.classroom.attendance.modules.chat.service.ChatService;
import com.classroom.attendance.modules.chat.websocket.ChatWebSocket;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController extends BaseController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> getConversations() {
        return Result.success(chatService.getConversations(currentUserId()));
    }

    @GetMapping("/messages")
    public Result<Page<ChatMessage>> getMessages(
            @RequestParam Long otherUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(chatService.getMessageThread(currentUserId(), otherUserId, pageNum, pageSize));
    }

    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@Valid @RequestBody SendMessageRequest dto) {
        ChatMessage msg = chatService.sendMessage(currentUserId(), dto.getReceiverId(), dto.getContent());
        ChatWebSocket.sendToUser(dto.getReceiverId(), Map.of("type", "new_message", "message", msg));
        return Result.success(msg);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(chatService.getUnreadCount(currentUserId()));
    }

    @PutMapping("/read/{otherUserId}")
    public Result<String> markAsRead(@PathVariable Long otherUserId) {
        chatService.markAsRead(currentUserId(), otherUserId);
        return Result.success("已标记");
    }

    @GetMapping("/search-users")
    public Result<List<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) String role) {
        return Result.success(chatService.searchUsers(currentUserId(), keyword, role));
    }

    @GetMapping("/suggested-contacts")
    public Result<List<User>> getSuggestedContacts() {
        return Result.success(chatService.getSuggestedContacts(currentUserId()));
    }
}
