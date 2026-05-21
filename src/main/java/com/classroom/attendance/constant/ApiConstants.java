package com.classroom.attendance.constant;

/**
 * API常量定义
 */
public class ApiConstants {
    
    /**
     * API版本
     */
    public static final String API_VERSION = "/api/v1";
    
    /**
     * 认证相关路径
     */
    public static final String AUTH_BASE = API_VERSION + "/auth";
    public static final String AUTH_LOGIN = AUTH_BASE + "/login";
    public static final String AUTH_REGISTER = AUTH_BASE + "/register";
    public static final String AUTH_LOGOUT = AUTH_BASE + "/logout";
    
    /**
     * 用户相关路径
     */
    public static final String USER_BASE = API_VERSION + "/user";
    
    /**
     * 学生相关路径
     */
    public static final String STUDENT_BASE = API_VERSION + "/student";
    
    /**
     * 班级相关路径
     */
    public static final String CLASS_BASE = API_VERSION + "/class";
    
    /**
     * 课程相关路径
     */
    public static final String COURSE_BASE = API_VERSION + "/course";
    
    /**
     * 考勤相关路径
     */
    public static final String ATTENDANCE_BASE = API_VERSION + "/attendance";
    
    /**
     * 行为记录相关路径
     */
    public static final String BEHAVIOR_BASE = API_VERSION + "/behavior";
    
    /**
     * 统计相关路径
     */
    public static final String STATISTICS_BASE = API_VERSION + "/statistics";
    
    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Token请求头
     */
    public static final String TOKEN_HEADER = "Authorization";
    
    private ApiConstants() {
        // 防止实例化
    }
}
