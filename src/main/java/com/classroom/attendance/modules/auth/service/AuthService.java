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
        user.setRole(req.getRole() != null ? req.getRole() : Constants.Role.STUDENT);
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
