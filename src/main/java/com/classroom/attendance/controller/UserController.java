package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.OperationLog;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.model.User;
import com.classroom.attendance.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequireRole("admin")
    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.list(page, size, role, keyword));
    }

    @RequireRole("admin")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @RequireRole("admin")
    @OperationLog(title = "创建用户", operation = "create")
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.fail("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.fail("密码不能为空");
        }
        return Result.success("创建成功", userService.register(user));
    }

    @RequireRole("admin")
    @OperationLog(title = "更新用户", operation = "update")
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return Result.success("更新成功");
    }

    @RequireRole("admin")
    @OperationLog(title = "重置用户密码", operation = "update")
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id, @RequestBody(required = false) String password) {
        String newPassword = (password != null && !password.isEmpty()) ? password : "123456";
        userService.updatePassword(id, newPassword);
        return Result.success("密码已重置为: " + newPassword);
    }

    @RequireRole("admin")
    @OperationLog(title = "删除用户", operation = "delete")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/roles")
    public Result<List<String>> getRoles() {
        return Result.success(List.of("admin", "teacher", "student"));
    }
}
