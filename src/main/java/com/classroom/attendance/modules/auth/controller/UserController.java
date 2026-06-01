package com.classroom.attendance.modules.auth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.OperationLog;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.auth.entity.User;
import com.classroom.attendance.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final AuthService authService;

    @RequireRole("admin")
    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        return Result.success(authService.listUsers(page, size, role, keyword));
    }

    @RequireRole("admin")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(authService.findById(id));
    }

    @RequireRole("admin")
    @OperationLog(title = "创建用户", operation = "create")
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        BusinessException.notNull(user.getUsername(), "用户名不能为空");
        BusinessException.notNull(user.getPassword(), "密码不能为空");
        return Result.success("创建成功", authService.registerUser(user));
    }

    @RequireRole("admin")
    @OperationLog(title = "更新用户", operation = "update")
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        authService.updateUser(user);
        return Result.success("更新成功");
    }

    @RequireRole("admin")
    @OperationLog(title = "重置用户密码", operation = "update")
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody(required = false) String password) {
        String newPassword = (password != null && !password.isEmpty()) ? password : "123456";
        authService.resetPassword(id, newPassword);
        return Result.success("密码已重置为: " + newPassword);
    }

    @RequireRole("admin")
    @OperationLog(title = "删除用户", operation = "delete")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        authService.deleteUser(id);
        return Result.success("删除成功");
    }

    @GetMapping("/roles")
    public Result<List<String>> getRoles() {
        return Result.success(List.of("admin", "teacher", "student"));
    }
}
