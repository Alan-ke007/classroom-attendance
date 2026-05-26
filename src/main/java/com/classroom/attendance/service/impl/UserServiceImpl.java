package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.User;
import com.classroom.attendance.mapper.UserMapper;
import com.classroom.attendance.service.UserService;
import com.classroom.attendance.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String storedPassword = user.getPassword();

        // 兼容旧数据：若密码不是BCrypt格式，尝试明文比对并自动迁移
        if (!storedPassword.startsWith("$2a$") && !storedPassword.startsWith("$2b$")) {
            if (!password.equals(storedPassword)) {
                throw new RuntimeException("用户名或密码错误");
            }
            user.setPassword(encoder.encode(password));
            userMapper.updateById(user);
        } else {
            if (!encoder.matches(password, storedPassword)) {
                throw new RuntimeException("用户名或密码错误");
            }
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }

    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public Page<User> list(Integer page, Integer size, String role, String keyword) {
        Page<User> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword));
        }

        wrapper.orderByDesc(User::getId);
        return userMapper.selectPage(pageObj, wrapper);
    }

    @Override
    public User register(User user) {
        User existingUser = getUserByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("student");
        }

        // 密码使用 BCrypt 加密存储
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(user.getPassword()));

        userMapper.insert(user);
        return user;
    }

    @Override
    public void update(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }

        // 不更新密码字段（单独接口处理）
        user.setPassword(null);
        userMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Override
    public User getUserByUsernameAndEmail(String username, String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getEmail, email);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setId(userId);
        user.setPassword(encoder.encode(newPassword));
        userMapper.updateById(user);
    }
}
