package com.classroom.attendance.filter;

import com.classroom.attendance.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JWT 认证过滤器 — 拦截 /api/* 请求，验证 Token
 */
@Slf4j
@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> EXCLUDED_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/captcha/generate",
        "/api/auth/forgot-password",
        "/api/auth/reset-password"
    );

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();
        String method = req.getMethod();

        // 放行预检请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // 放行公开路径
        if (EXCLUDED_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 仅拦截 /api/ 路径
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        String token = null;

        // 支持从 Header 或 URL 查询参数中获取 token（查询参数方式支持文件下载等场景）
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = req.getParameter("token");
        }

        if (token == null || token.isEmpty()) {
            writeUnauthorized(resp, "缺少认证令牌");
            return;
        }
        if (!jwtUtil.validateToken(token)) {
            writeUnauthorized(resp, "认证令牌无效或已过期");
            return;
        }

        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            Object uid = claims.get("userId");
            if (uid instanceof Integer) {
                req.setAttribute("userId", ((Integer) uid).longValue());
            } else {
                req.setAttribute("userId", uid);
            }
            req.setAttribute("username", claims.getSubject());
            req.setAttribute("role", claims.get("role"));
        } catch (Exception e) {
            writeUnauthorized(resp, "认证令牌解析失败");
            return;
        }

        chain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(200);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        String json = objectMapper.writeValueAsString(result);
        resp.getWriter().write(json);
    }
}
