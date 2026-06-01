package com.classroom.attendance.modules.log.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.log.entity.OperationLog;
import com.classroom.attendance.modules.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class OperationLogController extends BaseController {

    private final OperationLogService operationLogService;

    @RequireRole({"admin", "teacher"})
    @GetMapping("/list")
    public Result<Page<OperationLog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(operationLogService.list(page, size, username, operation, startDate, endDate));
    }

    @RequireRole({"admin"})
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        operationLogService.delete(id);
        return Result.success("ok");
    }

    @RequireRole({"admin"})
    @DeleteMapping("/clear")
    public Result<String> clear() {
        operationLogService.clearAll();
        return Result.success("ok");
    }
}
