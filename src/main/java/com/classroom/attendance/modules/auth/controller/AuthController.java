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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    // ② 安全：Cookie 属性（与 JWT 过期对齐）。Secure 仅在生产 HTTPS 下开启（默认关，兼容本地 HTTP 联调）。
    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @OperationLog(title = "用户登录", operation = "login")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req, HttpServletResponse response) {
        LoginResponse res = authService.login(req);
        // ② 安全：JWT 仅通过 httpOnly Cookie 下发，绝不写入响应体（JS 不可读，防 XSS 窃取）。
        addAuthCookie(response, res.getToken());
        res.setToken(null); // 响应体剔除 token，进一步缩小泄露面
        return Result.success(res);
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

        // C4 安全加固：限流 + 防枚举 + 令牌仅存哈希不下发；返回统一提示，不回显令牌。
        authService.requestPasswordReset(username, email);
        return Result.success("若账户存在，重置链接已发送");
    }

    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");
        BusinessException.isTrue(token != null && !token.isEmpty() && newPassword != null
                && newPassword.length() >= Constants.User.PASSWORD_MIN_LEN,
                "请填写重置令牌和新密码，密码至少6位");

        // C4：必须提供有效重置令牌，且仅能重置令牌绑定用户（服务端身份，忽略请求体 username/email）。
        authService.resetPasswordWithToken(token, newPassword);
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

    /**
     * ② 安全：退出登录。httpOnly Cookie 无法被前端 JS 清除，必须由服务端下发 Max-Age=0 使其失效。
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletResponse response) {
        clearAuthCookie(response);
        return Result.success("已退出登录");
    }

    @GetMapping("/info")
    public Result<UserResponse> getUserInfo(HttpServletRequest request) {
        // ② 兼容：优先读 Authorization 头（API 调用），兜底读 httpOnly Cookie（浏览器自动携带）。
        String token = resolveToken(request);
        BusinessException.notNull(token, "未登录或登录已过期");
        return Result.success(authService.getUserInfo(token));
    }

    // ===================== ② Cookie 辅助 =====================

    private void addAuthCookie(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie(token, false));
    }

    private void clearAuthCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("", true));
    }

    private String buildCookie(String value, boolean expire) {
        int maxAge = expire ? 0 : (int) (jwtExpirationMs / 1000);
        StringBuilder sb = new StringBuilder();
        sb.append("token=").append(value == null ? "" : value);
        sb.append("; Path=/");
        sb.append("; HttpOnly");
        sb.append("; SameSite=Lax");
        sb.append("; Max-Age=").append(maxAge);
        // SameSite=Lax 已覆盖本地同站（localhost 跨端口视为同站）场景；Secure 仅生产 HTTPS 开启。
        if (cookieSecure) {
            sb.append("; Secure");
        }
        return sb.toString();
    }

    private String resolveToken(HttpServletRequest request) {
        String auth = request.getHeader(Constants.TOKEN_HEADER);
        if (auth != null && auth.startsWith(Constants.TOKEN_PREFIX)) {
            return auth.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("token".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
