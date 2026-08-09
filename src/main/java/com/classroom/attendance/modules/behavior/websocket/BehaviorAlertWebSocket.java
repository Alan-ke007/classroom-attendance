package com.classroom.attendance.modules.behavior.websocket;

import com.classroom.attendance.infrastructure.util.JwtUtil;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint(value = "/ws/behavior-alert", configurator = JwtCookieHandshakeConfigurator.class)
public class BehaviorAlertWebSocket {

    private static final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static JwtUtil jwtUtil;
    private static ClassMapper classMapper;

    @Autowired
    public void setJwtUtil(JwtUtil util) { BehaviorAlertWebSocket.jwtUtil = util; }

    @Autowired
    public void setClassMapper(ClassMapper mapper) { BehaviorAlertWebSocket.classMapper = mapper; }

    private String sessionId;
    private Long userId;
    private String role;
    private String realName;
    private List<Long> classIds;

    @OnOpen
    public void onOpen(Session session) {
        this.sessionId = session.getId();
        try {
            // ② 安全：优先取握手配置器从 httpOnly Cookie 提取的 JWT；兜底兼容旧式 ?token= 查询参数。
            String token = (String) session.getUserProperties().get("token");
            if (token == null) {
                Map<String, List<String>> params = session.getRequestParameterMap();
                List<String> tokenParams = params.get("token");
                if (tokenParams == null || tokenParams.isEmpty()) {
                    session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Missing token"));
                    return;
                }
                token = tokenParams.get(0);
            }
            Claims claims = jwtUtil.getClaimsFromToken(token);
            this.userId = claims.get("userId", Long.class);
            this.role = claims.get("role", String.class);
            this.realName = claims.get("realName", String.class);
            if (this.userId == null) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "Invalid token"));
                return;
            }

            if ("teacher".equals(this.role) && classMapper != null) {
                this.classIds = classMapper.selectList(
                        new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getTeacher, this.realName))
                        .stream().map(ClassInfo::getId).toList();
            }

            sessions.put(session.getId(), new SessionInfo(session, userId, role, realName, classIds));
            sendMessage(session, Map.of("type", "connected", "message", "实时预警已连接",
                    "timestamp", System.currentTimeMillis()));
            log.info("BehaviorAlert WebSocket 连接: userId={}, role={}, classIds={}", userId, role, classIds);
        } catch (Exception e) {
            log.error("BehaviorAlert WebSocket 连接失败", e);
            try { session.close(); } catch (Exception ignored) {}
        }
    }

    @OnClose
    public void onClose() {
        if (sessionId != null) sessions.remove(sessionId);
        log.info("BehaviorAlert WebSocket 断开: userId={}", userId);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            sendMessage(session, Map.of("type", "pong"));
        } catch (IOException e) { log.error("回复pong失败", e); }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("BehaviorAlert WebSocket 错误: userId={}", userId, error);
    }

    public static void pushBehaviorAlert(Map<String, Object> alertData) {
        try {
            Map<String, Object> message = Map.of("type", "behavior_alert", "data", alertData,
                    "timestamp", System.currentTimeMillis());
            String json = objectMapper.writeValueAsString(message);

            Number classIdNum = (Number) alertData.get("classId");
            Long alertClassId = classIdNum != null ? classIdNum.longValue() : null;

            for (var entry : sessions.entrySet()) {
                SessionInfo info = entry.getValue();
                try {
                    if (!info.session.isOpen()) {
                        sessions.remove(entry.getKey());
                        continue;
                    }
                    if (shouldReceive(info, alertClassId)) {
                        info.session.getBasicRemote().sendText(json);
                    }
                } catch (IOException e) {
                    log.error("推送失败: {}", entry.getKey(), e);
                }
            }
        } catch (Exception e) { log.error("推送行为预警失败", e); }
    }

    private static boolean shouldReceive(SessionInfo info, Long alertClassId) {
        if ("admin".equals(info.role)) return true;
        if ("teacher".equals(info.role)) {
            if (alertClassId == null) return true;
            return info.classIds != null && info.classIds.contains(alertClassId);
        }
        return false;
    }

    public static int getSessionCount() { return sessions.size(); }

    private void sendMessage(Session session, Object data) throws IOException {
        session.getBasicRemote().sendText(objectMapper.writeValueAsString(data));
    }

    private record SessionInfo(Session session, Long userId, String role, String realName, List<Long> classIds) {}
}
