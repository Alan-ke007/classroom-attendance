package com.classroom.attendance.modules.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.OperationLog;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.constant.Constants;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.auth.dto.*;
import com.classroom.attendance.modules.auth.entity.User;
import com.classroom.attendance.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @OperationLog(title = "用户登录", operation = "login")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @OperationLog(title = "用户注册", operation = "create")
    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success("注册成功", authService.register(req));
    }

    @PostMapping("/forgot-password")
    public Result<String> forgotPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        BusinessException.isTrue(username != null && !username.isEmpty() && email != null && !email.isEmpty(),
                "请输入用户名和邮箱");

        User user = authService.findByUsername(username);
        BusinessException.isTrue(user != null && email.equals(user.getEmail()), "用户名与邮箱不匹配");
        return Result.success("身份验证通过");
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String newPassword = body.get("newPassword");
        BusinessException.isTrue(username != null && email != null && newPassword != null
                && !username.isEmpty() && !email.isEmpty() && newPassword.length() >= Constants.User.PASSWORD_MIN_LEN,
                "请填写完整信息，密码至少6位");

        User user = authService.findByUsername(username);
        BusinessException.isTrue(user != null && email.equals(user.getEmail()), "验证信息不匹配");
        authService.resetPassword(user.getId(), newPassword);
        return Result.success("密码重置成功");
    }

    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(@RequestBody UpdateProfileRequest req) {
        return Result.success("更新成功", authService.updateProfile(currentUserId(), req));
    }

    @PutMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(currentUserId(), req);
        return Result.success("密码修改成功");
    }

    @GetMapping("/info")
    public Result<UserResponse> getUserInfo(@RequestHeader(Constants.TOKEN_HEADER) String authorization) {
        String token = authorization.replace(Constants.TOKEN_PREFIX, "");
        return Result.success(authService.getUserInfo(token));
    }
}
