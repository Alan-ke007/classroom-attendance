package com.classroom.attendance.infrastructure.util;

import com.classroom.attendance.infrastructure.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        return (Long) getRequest().getAttribute("userId");
    }

    public static Long getCurrentStudentId() {
        return (Long) getRequest().getAttribute("studentId");
    }

    public static Long getCurrentClassId() {
        return (Long) getRequest().getAttribute("classId");
    }

    public static String getCurrentUsername() {
        return (String) getRequest().getAttribute("username");
    }

    public static String getCurrentRole() {
        return (String) getRequest().getAttribute("role");
    }

    public static String getCurrentUserRealName() {
        return (String) getRequest().getAttribute("realName");
    }

    public static Long requireUserId() {
        Long uid = getCurrentUserId();
        if (uid == null) throw new BusinessException(401, "未登录或登录已过期");
        return uid;
    }

    public static void requireAdmin() {
        if (!"admin".equals(getCurrentRole())) {
            throw new BusinessException(403, "权限不足，需要管理员角色");
        }
    }

    public static void requireTeacherOrAdmin() {
        String role = getCurrentRole();
        if (!"admin".equals(role) && !"teacher".equals(role)) {
            throw new BusinessException(403, "权限不足，需要教师或管理员角色");
        }
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) throw new BusinessException("无法获取请求上下文");
        return attrs.getRequest();
    }
}
