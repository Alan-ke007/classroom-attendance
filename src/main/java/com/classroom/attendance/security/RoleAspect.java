package com.classroom.attendance.security;

import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Aspect
@Component
public class RoleAspect {

    @Around("@annotation(com.classroom.attendance.infrastructure.annotation.RequireRole) || @within(com.classroom.attendance.infrastructure.annotation.RequireRole)")
    public Object checkRole(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String role = (String) request.getAttribute("role");
        BusinessException.notNull(401, role, "未获取到用户角色信息");

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        RequireRole methodAnno = signature.getMethod().getAnnotation(RequireRole.class);
        RequireRole classAnno = pjp.getTarget().getClass().getAnnotation(RequireRole.class);
        String[] requiredRoles = methodAnno != null ? methodAnno.value() : (classAnno != null ? classAnno.value() : null);

        if (requiredRoles != null && requiredRoles.length > 0) {
            List<String> roles = Arrays.asList(requiredRoles);
            if (!roles.contains(role)) {
                throw new BusinessException(403, "权限不足，需要 " + String.join("或", roles) + " 角色");
            }
        }
        return pjp.proceed();
    }
}
