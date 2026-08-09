package com.classroom.attendance.modules.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.constant.Constants;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.JwtUtil;
import com.classroom.attendance.modules.auth.dto.*;
import com.classroom.attendance.modules.auth.entity.User;
import com.classroom.attendance.modules.auth.mapper.UserMapper;
import com.classroom.attendance.modules.captcha.service.CaptchaService;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final CaptchaService captchaService;

    public LoginResponse login(LoginRequest req) {
        User user = findByUsername(req.getUsername());
        BusinessException.notNull(user, "用户名或密码错误");

        String stored = user.getPassword();
        boolean match = (stored.startsWith("$2a$") || stored.startsWith("$2b$"))
                ? passwordEncoder.matches(req.getPassword(), stored)
                : req.getPassword().equals(stored);

        BusinessException.isTrue(match, "用户名或密码错误");

        LoginResponse.LoginResponseBuilder builder = LoginResponse.builder()
                .token(null)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole());

        Long studentId = null;
        Long classId = null;
        if (Constants.Role.STUDENT.equals(user.getRole())) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, user.getId()));
            if (student != null) {
                studentId = student.getId();
                classId = student.getClassId();
                builder.studentId(studentId).classId(classId);
            }
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole(),
                user.getRealName(), studentId, classId);
        builder.token(token);

        return builder.build();
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        BusinessException.isTrue(captchaService.verify(req.getCaptchaId(), req.getCaptchaCode()), "验证码错误或已过期");
        BusinessException.isTrue(findByUsername(req.getUsername()) == null, "用户名已存在");

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRealName(req.getRealName());
        // 安全(C3)：公开注册忽略客户端传入的 role，强制为 STUDENT，防止自提权。
        user.setRole(Constants.Role.STUDENT);
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        userMapper.insert(user);

        if (Constants.Role.STUDENT.equals(user.getRole())) {
            Student student = new Student();
            student.setStudentNo(user.getUsername());
            student.setName(user.getRealName());
            student.setUserId(user.getId());
            studentMapper.insert(student);
        }

        return toUserResponse(user);
    }

    public UserResponse getUserInfo(String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        return toUserResponse(findByUsername(username));
    }

    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userMapper.selectById(userId);
        BusinessException.notNull(user, "用户不存在");

        if (req.getRealName() != null) user.setRealName(req.getRealName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());

        userMapper.updateById(user);
        return toUserResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = userMapper.selectById(userId);
        BusinessException.notNull(user, "用户不存在");

        String stored = user.getPassword();
        boolean match = (stored.startsWith("$2a$") || stored.startsWith("$2b$"))
                ? passwordEncoder.matches(req.getOldPassword(), stored)
                : req.getOldPassword().equals(stored);

        BusinessException.isTrue(match, "原密码错误");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userMapper.updateById(user);
    }

    public Page<User> listUsers(Integer page, Integer size, String role, String keyword) {
        Page<User> p = new Page<>(page, size);
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) w.eq(User::getRole, role);
        if (keyword != null && !keyword.isEmpty()) {
            w.and(wr -> wr.like(User::getUsername, keyword).or().like(User::getRealName, keyword));
        }
        w.orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(p, w);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    public User registerUser(User user) {
        BusinessException.isTrue(findByUsername(user.getUsername()) == null, "用户名已存在");
        user.setPassword(passwordEncoder.encode(
                user.getPassword() != null ? user.getPassword() : Constants.User.DEFAULT_PASSWORD));
        userMapper.insert(user);
        return user;
    }

    public void updateUser(User user) {
        User existing = userMapper.selectById(user.getId());
        BusinessException.notNull(existing, "用户不存在");
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
    }

    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        BusinessException.notNull(user, "用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /**
     * C4：生成无状态、签名的密码重置令牌，绑定 username，TTL 15 分钟。
     * 安全说明：真正安全应通过邮件/OTP 下发令牌并加限流；当前无邮件基础设施，
     * 仅完成结构性修复，令牌投递与限流为后续项（P1）。
     */
    public String createPasswordResetToken(String username, String email) {
        User user = findByUsername(username);
        BusinessException.isTrue(user != null && email != null && email.equals(user.getEmail()), "用户名与邮箱不匹配");
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "reset");
        claims.put("username", username);
        return jwtUtil.generateToken(claims, 15 * 60 * 1000L); // 15 分钟
    }

    /**
     * C4：校验重置令牌（有效签名 + 未过期 + purpose=reset），仅重置令牌绑定用户。
     * 备注：单次数使用需服务端 nonce/黑名单，留作 P1；15 分钟 TTL 已限制重放窗口。
     */
    public void resetPasswordWithToken(String token, String newPassword) {
        BusinessException.isTrue(newPassword != null && newPassword.length() >= Constants.User.PASSWORD_MIN_LEN,
                "请填写新密码，至少" + Constants.User.PASSWORD_MIN_LEN + "位");
        Map<String, Object> claims;
        try {
            claims = jwtUtil.parseToken(token); // 签名/过期校验在此抛出
        } catch (Exception e) {
            throw new BusinessException("重置令牌无效或已过期");
        }
        BusinessException.isTrue("reset".equals(claims.get("purpose")), "重置令牌无效");
        String username = (String) claims.get("username");
        BusinessException.notNull(username, "重置令牌无效");
        User user = findByUsername(username);
        BusinessException.notNull(user, "用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    public void deleteUser(Long id) {
        BusinessException.notNull(userMapper.selectById(id), "用户不存在");
        userMapper.deleteById(id);
    }

    public User findById(Long id) {
        return userMapper.selectById(id);
    }

    public User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    private UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId()).username(user.getUsername()).realName(user.getRealName())
                .role(user.getRole()).email(user.getEmail()).phone(user.getPhone())
                .avatar(user.getAvatar()).createTime(user.getCreateTime()).updateTime(user.getUpdateTime())
                .build();
    }
}
