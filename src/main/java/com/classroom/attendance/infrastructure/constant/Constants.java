package com.classroom.attendance.infrastructure.constant;

public final class Constants {

    private Constants() {}

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";

    public static final class ApiPath {
        public static final String LOGIN        = "/api/auth/login";
        public static final String REGISTER     = "/api/auth/register";
        public static final String FORGOT_PWD   = "/api/auth/forgot-password";
        public static final String RESET_PWD    = "/api/auth/reset-password";
        public static final String CAPTCHA      = "/api/captcha/generate";
        public static final String WS_BEHAVIOR  = "/ws/behavior";
        public static final String WS_CHAT      = "/ws/chat";

        private ApiPath() {}
    }

    public static final class Role {
        public static final String ADMIN   = "admin";
        public static final String TEACHER = "teacher";
        public static final String STUDENT = "student";

        private Role() {}
    }

    public static final class User {
        public static final String DEFAULT_PASSWORD = "123456";
        public static final int    PASSWORD_MIN_LEN = 6;

        private User() {}
    }

    public static final class File {
        public static final long MAX_SIZE     = 20 * 1024 * 1024;
        public static final long REQUEST_MAX  = 50 * 1024 * 1024;

        private File() {}
    }

    public static final class Attendance {
        public static final int EXPORT_PAGE_SIZE = 10000;

        private Attendance() {}
    }
}
