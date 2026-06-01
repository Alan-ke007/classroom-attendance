package com.classroom.attendance.modules.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.notification.entity.Notification;
import com.classroom.attendance.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController extends BaseController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public Result<Page<Notification>> getList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(notificationService.getUserNotifications(currentUserId(), pageNum, pageSize));
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount(currentUserId()));
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("ok");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead() {
        notificationService.markAllAsRead(currentUserId());
        return Result.success("ok");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return Result.success("ok");
    }
}
