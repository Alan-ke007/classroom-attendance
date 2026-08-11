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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
    private final PasswordResetTokenMapper resetTokenMapper;

    // C4 限流：内存固定窗口（演示用，单实例；多实例应换 Redis）。按客户端 IP 计数。
    private final ConcurrentHashMap<String, Deque<Long>> resetRateLimit = new ConcurrentHashMap<>();
    private static final int RESET_MAX_PER_WINDOW = 5;
    private static final long RESET_WINDOW_MS = 60_000L;

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
     * C4（安全加固，无外部基建）：申请密码重置。
     * - 按客户端 IP 限流（60s 内最多 5 次），防爆破。
     * - 防用户枚举：username/email 不匹配也返回同样的"已发送"提示，不暴露账户是否存在。
     * - 生成随机令牌，服务端仅存 SHA-256 哈希（一次性 + 15 分钟 TTL），原始令牌不下发前端。
     *   真实投递（邮件/OTP）为后续项；当前仅在 DEBUG 日志打印原始令牌便于本地联调。
     */
    public void requestPasswordReset(String username, String email) {
        BusinessException.isTrue(username != null && !username.isEmpty()
                && email != null && !email.isEmpty(), "请输入用户名和邮箱");
        assertResetNotRateLimited();
        User user = findByUsername(username);
        boolean valid = user != null && email.equals(user.getEmail());
        if (!valid) {
            return; // 统一返回，不暴露账户是否存在
        }
        String raw = generateRawToken();
        String hash = sha256Hex(raw);
        LocalDateTime now = LocalDateTime.now();
        resetTokenMapper.insert(PasswordResetToken.builder()
                .username(username).tokenHash(hash)
                .expiry(now.plusMinutes(15)).used(0).createTime(now).build());
        if (log.isDebugEnabled()) {
            log.debug("C4 本地联调重置令牌(仅 DEBUG，生产不应打印): {}", raw);
        }
    }

    /**
     * C4（安全加固）：凭重置令牌改密。令牌为一次性 + 15 分钟 TTL，校验失败即用即废。
     * 事务：令牌置废与用户改密跨两张表，须原子执行，避免“校验通过但改密失败、令牌却未作废”被并发重试利用。
     */
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        BusinessException.isTrue(newPassword != null && newPassword.length() >= Constants.User.PASSWORD_MIN_LEN,
                "请填写新密码，至少" + Constants.User.PASSWORD_MIN_LEN + "位");
        String hash = sha256Hex(token);
        PasswordResetToken record = resetTokenMapper.selectOne(new LambdaQueryWrapper<PasswordResetToken>()
                .eq(PasswordResetToken::getTokenHash, hash).eq(PasswordResetToken::getUsed, 0));
        BusinessException.notNull(record, "重置令牌无效或已过期");
        if (record.getExpiry().isBefore(LocalDateTime.now())) {
            resetTokenMapper.deleteById(record.getId());
            throw new BusinessException("重置令牌无效或已过期");
        }
        // 一次性：立即作废
        record.setUsed(1);
        resetTokenMapper.updateById(record);

        User user = findByUsername(record.getUsername());
        BusinessException.notNull(user, "用户不存在");
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    // ---- C4 辅助 ----

    private void assertResetNotRateLimited() {
        String ip = clientIp();
        Deque<Long> hits = resetRateLimit.computeIfAbsent(ip, k -> new ArrayDeque<>());
        long now = System.currentTimeMillis();
        synchronized (hits) {
            while (!hits.isEmpty() && now - hits.peekFirst() > RESET_WINDOW_MS) hits.pollFirst();
            if (hits.size() >= RESET_MAX_PER_WINDOW) {
                throw new BusinessException("操作过于频繁，请稍后再试");
            }
            hits.addLast(now);
        }
    }

    private String clientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "unknown";
            HttpServletRequest req = attrs.getRequest();
            String fwd = req.getHeader("X-Forwarded-For");
            if (fwd != null && !fwd.isEmpty()) return fwd.split(",")[0].trim();
            return req.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

    String generateRawToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException("令牌处理失败");
        }
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
