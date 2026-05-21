package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.dto.SendMessageDTO;
import com.classroom.attendance.mapper.UserMapper;
import com.classroom.attendance.model.ChatMessage;
import com.classroom.attendance.model.User;
import com.classroom.attendance.service.ChatService;
import com.classroom.attendance.vo.ConversationVO;
import com.classroom.attendance.websocket.ChatWebSocket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> getConversations(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(chatService.getConversations(userId));
    }

    @GetMapping("/messages")
    public Result<Page<ChatMessage>> getMessages(
            @RequestParam Long otherUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        Page<ChatMessage> page = chatService.getMessageThread(userId, otherUserId, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/send")
    public Result<ChatMessage> sendMessage(@RequestBody SendMessageDTO dto, HttpServletRequest request) {
        Long userId = getUserId(request);
        try {
            ChatMessage msg = chatService.sendMessage(userId, dto.getReceiverId(), dto.getContent());
            // 推送 WebSocket 通知给接收者
            ChatWebSocket.sendToUser(dto.getReceiverId(), Map.of("type", "new_message", "message", msg));
            return Result.success(msg);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        return Result.success(chatService.getUnreadCount(userId));
    }

    @PutMapping("/read/{otherUserId}")
    public Result<String> markAsRead(@PathVariable Long otherUserId, HttpServletRequest request) {
        Long userId = getUserId(request);
        chatService.markAsRead(userId, otherUserId);
        return Result.success("已标记");
    }

    @GetMapping("/search-users")
    public Result<List<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) String role,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(User::getId, userId); // 排除自己
        wrapper.and(w ->
                w.like(User::getUsername, keyword)
                 .or().like(User::getRealName, keyword)
        );
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.last("limit 20");
        List<User> users = userMapper.selectList(wrapper);
        // 脱敏：不返回密码
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    private Long getUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        if (attr == null) throw new RuntimeException("无法获取用户身份信息");
        return attr instanceof Long ? (Long) attr : ((Number) attr).longValue();
    }
}
