package com.classroom.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.NotificationMapper;
import com.classroom.attendance.model.Notification;
import com.classroom.attendance.websocket.BehaviorAlertWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 创建通知并推送到WebSocket
     */
    public Notification createAndPush(Long userId, String type, String title, String content, Long refId) {
        Notification notif = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .isRead(0)
                .refId(refId)
                .build();
        notificationMapper.insert(notif);

        // 推送WebSocket通知
        try {
            BehaviorAlertWebSocket.pushBehaviorAlert(Map.of(
                    "type", "notification",
                    "notificationId", notif.getId(),
                    "notificationType", type,
                    "title", title,
                    "content", content
            ));
        } catch (Exception e) {
            log.warn("WebSocket推送失败，通知已保存: {}", e.getMessage());
        }

        return notif;
    }

    /**
     * 获取用户通知列表
     */
    public Page<Notification> getUserNotifications(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        return notificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 获取未读数量
     */
    public long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }

    /**
     * 标记为已读
     */
    public void markAsRead(Long id) {
        Notification notif = notificationMapper.selectById(id);
        if (notif != null) {
            notif.setIsRead(1);
            notificationMapper.updateById(notif);
        }
    }

    /**
     * 全部标记已读
     */
    public void markAllAsRead(Long userId) {
        Notification update = Notification.builder().isRead(1).build();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        notificationMapper.update(update, wrapper);
    }
}
