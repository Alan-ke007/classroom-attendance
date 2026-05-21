package com.classroom.attendance.controller;

import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.dto.LoginReq;
import com.classroom.attendance.dto.RegisterReq;
import com.classroom.attendance.mapper.UserMapper;
import com.classroom.attendance.model.User;
import com.classroom.attendance.service.CaptchaService;
import com.classroom.attendance.service.StudentService;
import com.classroom.attendance.service.UserService;
import com.classroom.attendance.utils.JwtUtil;
import com.classroom.attendance.vo.LoginVO;
import com.classroom.attendance.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginReq req) {
        String token = userService.login(req.getUsername(), req.getPassword());
        User user = userService.getUserByUsername(req.getUsername());
        LoginVO.LoginVOBuilder builder = LoginVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole());
        if ("student".equals(user.getRole())) {
            var student = studentService.getStudentByUserId(user.getId());
            if (student != null) {
                builder.studentId(student.getId())
                       .classId(student.getClassId());
            }
        }
        return Result.success(builder.build());
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterReq req) {
        if (!captchaService.verify(req.getCaptchaId(), req.getCaptchaCode())) {
            return Result.error("验证码错误或已过期");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());
        user.setRealName(req.getRealName());
        user.setRole(req.getRole() != null ? req.getRole() : "student");
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());

        User registered = userService.register(user);

        // 学生注册时自动创建 student 记录
        if ("student".equals(registered.getRole())) {
            com.classroom.attendance.model.Student student = new com.classroom.attendance.model.Student();
            student.setStudentNo(registered.getUsername());
            student.setName(registered.getRealName());
            student.setUserId(registered.getId());
            studentService.addStudent(student);
        }

        return Result.success("注册成功", toUserVO(registered));
    }

    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        if (username == null || email == null || username.isEmpty() || email.isEmpty()) {
            return Result.error("请输入用户名和邮箱");
        }
        User user = userService.getUserByUsernameAndEmail(username, email);
        if (user == null) {
            return Result.error("用户名与邮箱不匹配");
        }
        return Result.success("身份验证通过");
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String newPassword = body.get("newPassword");
        if (username == null || email == null || newPassword == null ||
            username.isEmpty() || email.isEmpty() || newPassword.length() < 6) {
            return Result.error("请填写完整信息，密码至少6位");
        }
        User user = userService.getUserByUsernameAndEmail(username, email);
        if (user == null) {
            return Result.error("验证信息不匹配");
        }
        userService.updatePassword(user.getId(), newPassword);
        return Result.success("密码重置成功");
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail("用户不存在");

        if (body.containsKey("realName")) user.setRealName(body.get("realName"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));

        userMapper.updateById(user);
        return Result.success("更新成功", toUserVO(user));
    }

    @PutMapping("/password")
    public Result<String> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) return Result.fail("请输入原密码");
        if (newPassword == null || newPassword.length() < 6) return Result.fail("新密码至少6位");

        User user = userMapper.selectById(userId);
        if (user == null) return Result.fail("用户不存在");

        String stored = user.getPassword();
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
            if (!passwordEncoder.matches(oldPassword, stored)) return Result.fail("原密码错误");
        } else {
            if (!oldPassword.equals(stored)) return Result.fail("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return Result.success("密码修改成功");
    }

    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        User user = userService.getUserByUsername(username);
        return Result.success(toUserVO(user));
    }

    private UserVO toUserVO(User user) {
        if (user == null) return null;
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }
}
