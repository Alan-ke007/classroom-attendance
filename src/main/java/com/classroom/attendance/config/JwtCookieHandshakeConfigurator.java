package com.classroom.attendance.config;

import jakarta.websocket.HandshakeRequest;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.List;
import java.util.Map;

/**
 * ② 安全：WebSocket 握手配置器。
 * 浏览器在 WS 握手时自动携带 httpOnly Cookie（与 REST 同源），此处从中提取 JWT 并暂存到
 * 握手用户属性；真正的鉴权校验仍由 @OnOpen 用既有的 JwtUtil/secretKey 完成（单一校验源）。
 * 兼容旧式 ?token= 查询参数（非浏览器/脚本客户端）；两者皆无则交由 @OnOpen 拒绝连接。
 */
public class JwtCookieHandshakeConfigurator extends ServerEndpointConfig.Configurator {

    private static final String COOKIE_NAME = "token";

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        String token = extractTokenFromCookie(request);
        if (token == null) {
            // 兜底：查询参数 ?token=（旧前端/非浏览器客户端）
            Map<String, List<String>> params = request.getParameterMap();
            List<String> tp = params.get("token");
            token = (tp != null && !tp.isEmpty()) ? tp.get(0) : null;
        }
        if (token != null) {
            sec.getUserProperties().put("token", token);
        }
    }

    private String extractTokenFromCookie(HandshakeRequest request) {
        List<String> cookies = request.getHeaders().get("Cookie");
        if (cookies == null) return null;
        for (String header : cookies) {
            for (String part : header.split(";")) {
                part = part.trim();
                if (part.startsWith(COOKIE_NAME + "=")) {
                    return part.substring((COOKIE_NAME + "=").length());
                }
            }
        }
        return null;
    }
}
