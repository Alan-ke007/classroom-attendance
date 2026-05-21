package com.classroom.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.ChatMessageMapper;
import com.classroom.attendance.mapper.UserMapper;
import com.classroom.attendance.model.ChatMessage;
import com.classroom.attendance.model.User;
import com.classroom.attendance.vo.ConversationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("不能给自己发送消息");
        }
        User receiver = userMapper.selectById(receiverId);
        if (receiver == null) {
            throw new RuntimeException("接收者不存在");
        }
        ChatMessage msg = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .isRead(0)
                .build();
        chatMessageMapper.insert(msg);
        log.info("消息已发送: sender={}, receiver={}, id={}", senderId, receiverId, msg.getId());
        return msg;
    }

    public List<ConversationVO> getConversations(Long userId) {
        List<Long> otherUserIds = chatMessageMapper.selectConversationUserIds(userId);

        List<ConversationVO> conversations = new ArrayList<>();
        for (Long otherId : otherUserIds) {
            User other = userMapper.selectById(otherId);
            if (other == null) continue;

            // 获取最后一条消息
            LambdaQueryWrapper<ChatMessage> lastMsgWrapper = new LambdaQueryWrapper<>();
            lastMsgWrapper.and(w ->
                    w.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, otherId)
            ).or(w ->
                    w.eq(ChatMessage::getSenderId, otherId).eq(ChatMessage::getReceiverId, userId)
            );
            lastMsgWrapper.orderByDesc(ChatMessage::getCreateTime).last("limit 1");
            ChatMessage lastMsg = chatMessageMapper.selectList(lastMsgWrapper).stream().findFirst().orElse(null);

            // 未读计数（对方发来且未读）
            LambdaQueryWrapper<ChatMessage> unreadWrapper = new LambdaQueryWrapper<>();
            unreadWrapper.eq(ChatMessage::getSenderId, otherId)
                    .eq(ChatMessage::getReceiverId, userId)
                    .eq(ChatMessage::getIsRead, 0);
            Long unreadCount = chatMessageMapper.selectCount(unreadWrapper);

            conversations.add(ConversationVO.builder()
                    .otherUserId(other.getId())
                    .otherUserName(other.getRealName() != null ? other.getRealName() : other.getUsername())
                    .otherUserAvatar(other.getAvatar())
                    .otherUserRole(other.getRole())
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                    .lastMessageTime(lastMsg != null ? lastMsg.getCreateTime() : null)
                    .unreadCount(unreadCount)
                    .build());
        }

        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });
        return conversations;
    }

    public Page<ChatMessage> getMessageThread(Long userId, Long otherUserId, int pageNum, int pageSize) {
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w ->
                w.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, otherUserId)
        ).or(w ->
                w.eq(ChatMessage::getSenderId, otherUserId).eq(ChatMessage::getReceiverId, userId)
        );
        wrapper.orderByDesc(ChatMessage::getCreateTime);

        Page<ChatMessage> result = chatMessageMapper.selectPage(page, wrapper);

        // 填充发送者姓名和头像
        User sender1 = userMapper.selectById(userId);
        User sender2 = userMapper.selectById(otherUserId);
        for (ChatMessage m : result.getRecords()) {
            if (m.getSenderId().equals(userId)) {
                m.setSenderName(sender1 != null && sender1.getRealName() != null ? sender1.getRealName() : sender1 != null ? sender1.getUsername() : "");
                m.setSenderAvatar(sender1 != null ? sender1.getAvatar() : null);
            } else {
                m.setSenderName(sender2 != null && sender2.getRealName() != null ? sender2.getRealName() : sender2 != null ? sender2.getUsername() : "");
                m.setSenderAvatar(sender2 != null ? sender2.getAvatar() : null);
            }
        }
        return result;
    }

    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getReceiverId, userId).eq(ChatMessage::getIsRead, 0);
        return chatMessageMapper.selectCount(wrapper);
    }

    public Long getUnreadCountByUser(Long userId, Long otherUserId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSenderId, otherUserId)
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0);
        return chatMessageMapper.selectCount(wrapper);
    }

    public void markAsRead(Long userId, Long otherUserId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSenderId, otherUserId)
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0);

        ChatMessage updated = new ChatMessage();
        updated.setIsRead(1);
        // 批量更新，避免逐条 updateById
        chatMessageMapper.update(updated, wrapper);
    }
}
