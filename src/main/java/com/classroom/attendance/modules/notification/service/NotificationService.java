package com.classroom.attendance.modules.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.modules.notification.entity.Notification;
import com.classroom.attendance.modules.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public Page<Notification> getUserNotifications(Long userId, int pageNum, int pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        return notificationMapper.selectPage(page,
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId)
                        .orderByDesc(Notification::getCreateTime));
    }

    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0));
    }

    public void markAsRead(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null) { n.setIsRead(1); notificationMapper.updateById(n); }
    }

    public void markAllAsRead(Long userId) {
        Notification entity = new Notification();
        entity.setIsRead(1);
        notificationMapper.update(entity,
                new LambdaQueryWrapper<Notification>().eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0));
    }

    public void delete(Long id) {
        notificationMapper.deleteById(id);
    }
}
