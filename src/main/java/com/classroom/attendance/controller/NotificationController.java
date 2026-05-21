package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.model.Notification;
import com.classroom.attendance.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/list")
    public Result<Page<Notification>> getList(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = getUserId(request);
        Page<Notification> page = notificationService.getUserNotifications(userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        Long userId = getUserId(request);
        long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/read/{id}")
    public Result<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success("ok");
    }

    @PutMapping("/read-all")
    public Result<String> markAllAsRead(HttpServletRequest request) {
        Long userId = getUserId(request);
        notificationService.markAllAsRead(userId);
        return Result.success("ok");
    }

    private Long getUserId(HttpServletRequest request) {
        Object attr = request.getAttribute("userId");
        if (attr == null) throw new RuntimeException("无法获取用户身份信息");
        return attr instanceof Long ? (Long) attr : ((Number) attr).longValue();
    }
}
