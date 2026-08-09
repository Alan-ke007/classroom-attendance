package com.classroom.attendance.infrastructure.base;

import com.classroom.attendance.infrastructure.util.SecurityUtil;

public abstract class BaseController {

    protected Long currentUserId() {
        return SecurityUtil.requireUserId();
    }

    protected Long currentUserIdOrNull() {
        return SecurityUtil.getCurrentUserId();
    }

    protected String currentRole() {
        return SecurityUtil.getCurrentRole();
    }

    protected String currentUsername() {
        return SecurityUtil.getCurrentUsername();
    }
}
