package com.classroom.attendance.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解 — 标注在 Controller 方法上限制访问角色
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value() default {};
}
