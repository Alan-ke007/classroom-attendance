package com.classroom.attendance.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务端点 - 实时预警推送
 */
@Slf4j
@Component
@ServerEndpoint("/ws/behavior-alert")
public class BehaviorAlertWebSocket {
    
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket连接建立，sessionId: {}, 当前连接数: {}", sessionId, sessions.size());
        
        // 发送欢迎消息
        try {
            sendMessage(sessionId, Map.of(
                "type", "connected",
                "message", "WebSocket连接成功",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (IOException e) {
            log.error("发送欢迎消息失败", e);
        }
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("WebSocket连接关闭，sessionId: {}, 当前连接数: {}", sessionId, sessions.size());
    }
    
    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到客户端消息，sessionId: {}, message: {}", session.getId(), message);
        
        // 可以处理客户端发来的消息，如心跳等
        try {
            sendMessage(session.getId(), Map.of(
                "type", "pong",
                "message", "收到消息",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (IOException e) {
            log.error("回复消息失败", e);
        }
    }
    
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误，sessionId: {}", session.getId(), error);
    }
    
    /**
     * 向指定会话发送消息
     */
    public void sendMessage(String sessionId, Object data) throws IOException {
        Session session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(data);
            session.getBasicRemote().sendText(jsonMessage);
        }
    }
    
    /**
     * 广播消息给所有连接的客户端
     */
    public void broadcastMessage(Object data) throws IOException {
        String jsonMessage = objectMapper.writeValueAsString(data);
        for (Map.Entry<String, Session> entry : sessions.entrySet()) {
            Session session = entry.getValue();
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(jsonMessage);
                } catch (IOException e) {
                    log.error("广播消息失败，sessionId: {}", entry.getKey(), e);
                }
            }
        }
    }
    
    /**
     * 推送行为预警消息
     */
    public static void pushBehaviorAlert(Map<String, Object> alertData) {
        try {
            Map<String, Object> message = Map.of(
                "type", "behavior_alert",
                "data", alertData,
                "timestamp", System.currentTimeMillis()
            );
            String jsonMessage = objectMapper.writeValueAsString(message);
            for (Map.Entry<String, Session> entry : sessions.entrySet()) {
                Session session = entry.getValue();
                if (session != null && session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(jsonMessage);
                    } catch (IOException e) {
                        log.error("推送预警消息失败，sessionId: {}", entry.getKey(), e);
                    }
                }
            }
            log.info("行为预警已推送到 {} 个客户端: {}", sessions.size(), alertData);
        } catch (Exception e) {
            log.error("推送行为预警失败", e);
        }
    }
    
    /**
     * 获取当前连接数
     */
    public static int getSessionCount() {
        return sessions.size();
    }
}
