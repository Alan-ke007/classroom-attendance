package com.classroom.attendance.service;

import com.classroom.attendance.model.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 注册新用户
     */
    User register(User user);

    /**
     * 根据用户名和邮箱查询用户
     */
    User getUserByUsernameAndEmail(String username, String email);

    /**
     * 更新用户密码
     */
    void updatePassword(Long userId, String newPassword);
}
