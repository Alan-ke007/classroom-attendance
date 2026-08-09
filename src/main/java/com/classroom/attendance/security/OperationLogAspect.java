package com.classroom.attendance.security;

import com.classroom.attendance.modules.log.entity.OperationLog;
import com.classroom.attendance.modules.log.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(com.classroom.attendance.infrastructure.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;
        boolean success = true;
        String errorMsg = null;

        try {
            result = point.proceed();
        } catch (Throwable e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            try { saveLog(point, System.currentTimeMillis() - startTime, success, errorMsg); }
            catch (Exception e) { log.error("保存操作日志失败", e); }
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, long costTime, boolean success, String errorMsg) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        com.classroom.attendance.infrastructure.annotation.OperationLog annotation =
                method.getAnnotation(com.classroom.attendance.infrastructure.annotation.OperationLog.class);

        OperationLog logEntry = new OperationLog();
        logEntry.setTitle(annotation.title());
        logEntry.setOperation(annotation.operation());
        logEntry.setMethod(point.getTarget().getClass().getName() + "." + method.getName());
        logEntry.setCostTime(costTime);
        logEntry.setStatus(success ? 1 : 0);
        logEntry.setErrorMsg(errorMsg);

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                logEntry.setIp(getIpAddress(request));
                Object userId = request.getAttribute("userId");
                if (userId != null) logEntry.setUserId(Long.parseLong(userId.toString()));
                Object username = request.getAttribute("username");
                if (username != null) logEntry.setUsername(username.toString());

                try {
                    Object[] args = point.getArgs();
                    if (args != null && args.length > 0) {
                        String params = objectMapper.writeValueAsString(args);
                        logEntry.setRequestParams(params.length() > 1000 ? params.substring(0, 1000) + "..." : params);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception e) { log.warn("获取请求信息失败", e); }

        operationLogMapper.insert(logEntry);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getHeader("WL-Proxy-Client-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = request.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }
}
