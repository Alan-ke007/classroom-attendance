package com.classroom.attendance.websocket;

import com.classroom.attendance.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/chat")
public class ChatWebSocket {

    private static final Map<Long, Session> userSessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static SecretKey secretKey;

    private static ChatService chatService;

    @Autowired
    public void setChatService(ChatService service) {
        ChatWebSocket.chatService = service;
    }

    @Autowired
    public void setJwtSecret(@Value("${jwt.secret}") String secret) {
        ChatWebSocket.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private Long currentUserId;

    @OnOpen
    public void onOpen(Session session) {
        try {
            Map<String, List<String>> params = session.getRequestParameterMap();
            List<String> tokenParams = params.get("token");
            if (tokenParams == null || tokenParams.isEmpty()) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Missing token"));
                return;
            }
            String token = tokenParams.get(0);
            Claims claims = Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).getPayload();
            this.currentUserId = claims.get("userId", Long.class);
            if (this.currentUserId == null) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid token"));
                return;
            }
            // 替换旧连接
            Session old = userSessions.put(this.currentUserId, session);
            if (old != null && old.isOpen()) {
                try { old.close(); } catch (Exception ignored) {}
            }
            log.info("Chat WebSocket 连接: userId={}", this.currentUserId);
            sendToSession(session, Map.of("type", "connected", "message", "聊天服务已连接"));
        } catch (Exception e) {
            log.error("Chat WebSocket 连接失败", e);
            try { session.close(); } catch (Exception ignored) {}
        }
    }

    @OnClose
    public void onClose() {
        if (currentUserId != null) {
            userSessions.remove(currentUserId);
            log.info("Chat WebSocket 断开: userId={}", currentUserId);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            Map<String, Object> data = objectMapper.readValue(message, Map.class);
            String type = (String) data.get("type");

            if ("ping".equals(type)) {
                sendToSession(userSessions.get(currentUserId), Map.of("type", "pong"));
            } else if ("chat_message".equals(type)) {
                Number receiverIdNum = (Number) data.get("receiverId");
                String content = (String) data.get("content");
                Number clientMsgIdNum = (Number) data.get("clientMsgId");
                if (receiverIdNum == null || content == null || content.trim().isEmpty()) return;
                Long receiverId = receiverIdNum.longValue();
                // 持久化
                var msg = chatService.sendMessage(currentUserId, receiverId, content.trim());
                // 确认给发送者（带回客户端消息ID用于匹配）
                Map<String, Object> ackPayload = new HashMap<>(Map.of("type", "chat_message_ack", "message", msg));
                if (clientMsgIdNum != null) {
                    ackPayload.put("clientMsgId", clientMsgIdNum.longValue());
                }
                sendToUser(currentUserId, ackPayload);
                // 推送给接收者
                sendToUser(receiverId, Map.of("type", "new_message", "message", msg));
            } else if ("mark_read".equals(type)) {
                Number otherIdNum = (Number) data.get("otherUserId");
                if (otherIdNum != null) {
                    chatService.markAsRead(currentUserId, otherIdNum.longValue());
                    sendToUser(currentUserId, Map.of("type", "mark_read_ack", "otherUserId", otherIdNum));
                }
            }
        } catch (Exception e) {
            log.error("Chat WebSocket 消息处理失败", e);
        }
    }

    @OnError
    public void onError(Throwable error) {
        log.error("Chat WebSocket 错误: userId={}", currentUserId, error);
    }

    public static void sendToUser(Long userId, Object data) {
        Session session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            sendToSession(session, data);
        }
    }

    private static void sendToSession(Session session, Object data) {
        try {
            session.getBasicRemote().sendText(objectMapper.writeValueAsString(data));
        } catch (Exception e) {
            log.error("发送消息失败", e);
        }
    }
}
