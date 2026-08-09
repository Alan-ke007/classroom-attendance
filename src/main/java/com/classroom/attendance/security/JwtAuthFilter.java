package com.classroom.attendance.security;

import com.classroom.attendance.infrastructure.constant.Constants;
import com.classroom.attendance.infrastructure.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {

    private final JwtUtil jwtUtil;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> EXCLUDED_PATHS = List.of(
            Constants.ApiPath.LOGIN, Constants.ApiPath.REGISTER, Constants.ApiPath.CAPTCHA,
            Constants.ApiPath.FORGOT_PWD, Constants.ApiPath.RESET_PWD, Constants.ApiPath.LOGOUT);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(req.getMethod()) || EXCLUDED_PATHS.contains(path) || !path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        String token = extractToken(req);
        if (token == null || !jwtUtil.validateToken(token)) {
            writeUnauthorized(resp, token == null ? "缺少认证令牌" : "认证令牌无效或已过期");
            return;
        }

        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            Object uid = claims.get("userId");
            req.setAttribute("userId", uid instanceof Integer ? ((Integer) uid).longValue() : uid);
            req.setAttribute("username", claims.getSubject());
            req.setAttribute("role", claims.get("role"));
            req.setAttribute("realName", claims.get("realName"));
            Object sid = claims.get("studentId");
            if (sid != null) {
                req.setAttribute("studentId", sid instanceof Integer ? ((Integer) sid).longValue() : sid);
            }
            Object cid = claims.get("classId");
            if (cid != null) {
                req.setAttribute("classId", cid instanceof Integer ? ((Integer) cid).longValue() : cid);
            }
        } catch (Exception e) {
            writeUnauthorized(resp, "认证令牌解析失败");
            return;
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest req) {
        String authHeader = req.getHeader(Constants.TOKEN_HEADER);
        if (authHeader != null && authHeader.startsWith(Constants.TOKEN_PREFIX)) {
            return authHeader.substring(7);
        }
        // ② 安全：httpOnly Cookie 兜底（JS 不可读，防 XSS 窃取；浏览器自动随同源请求携带）
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("token".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return req.getParameter("token");
    }

    private void writeUnauthorized(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(200);
        resp.getWriter().write(objectMapper.writeValueAsString(
                Map.of("code", 401, "message", message)));
    }
}
