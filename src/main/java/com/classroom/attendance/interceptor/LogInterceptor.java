package com.classroom.attendance.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 请求日志拦截器 — 记录请求方法、URI、IP 和处理耗时
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "_requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String ip = request.getRemoteAddr();
        int status = response.getStatus();

        if (query != null) {
            log.info("[{}] {}?{} | IP: {} | 状态: {} | 耗时: {}ms", method, uri, query, ip, status, duration);
        } else {
            log.info("[{}] {} | IP: {} | 状态: {} | 耗时: {}ms", method, uri, ip, status, duration);
        }
    }
}
