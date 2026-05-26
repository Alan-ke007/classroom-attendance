package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
     * 根据ID查询用户
     */
    User getById(Long id);

    /**
     * 分页查询用户列表
     */
    Page<User> list(Integer page, Integer size, String role, String keyword);

    /**
     * 注册新用户
     */
    User register(User user);

    /**
     * 更新用户
     */
    void update(User user);

    /**
     * 删除用户（逻辑删除）
     */
    void delete(Long id);

    /**
     * 根据用户名和邮箱查询用户
     */
    User getUserByUsernameAndEmail(String username, String email);

    /**
     * 更新用户密码
     */
    void updatePassword(Long userId, String newPassword);
}
