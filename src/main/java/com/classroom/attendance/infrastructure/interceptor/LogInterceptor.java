package com.classroom.attendance.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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
        String query = request.getQueryString();
        if (query != null) {
            log.info("[{}] {}?{} | IP: {} | 状态: {} | 耗时: {}ms",
                    request.getMethod(), request.getRequestURI(), query, request.getRemoteAddr(),
                    response.getStatus(), duration);
        } else {
            log.info("[{}] {} | IP: {} | 状态: {} | 耗时: {}ms",
                    request.getMethod(), request.getRequestURI(), request.getRemoteAddr(),
                    response.getStatus(), duration);
        }
    }
}
