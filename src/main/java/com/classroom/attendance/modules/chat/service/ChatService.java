package com.classroom.attendance.modules.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.modules.auth.entity.User;
import com.classroom.attendance.modules.auth.mapper.UserMapper;
import com.classroom.attendance.modules.chat.dto.ConversationResponse;
import com.classroom.attendance.modules.chat.entity.ChatMessage;
import com.classroom.attendance.modules.chat.mapper.ChatMessageMapper;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;

    public ChatMessage sendMessage(Long senderId, Long receiverId, String content) {
        BusinessException.isTrue(!senderId.equals(receiverId), "不能给自己发送消息");
        BusinessException.notNull(userMapper.selectById(receiverId), "接收者不存在");

        ChatMessage msg = ChatMessage.builder()
                .senderId(senderId).receiverId(receiverId)
                .content(content).isRead(0).build();
        chatMessageMapper.insert(msg);
        return msg;
    }

    public List<ConversationResponse> getConversations(Long userId) {
        List<Long> otherIds = chatMessageMapper.selectConversationUserIds(userId);
        List<ConversationResponse> conversations = new ArrayList<>();

        for (Long otherId : otherIds) {
            User other = userMapper.selectById(otherId);
            if (other == null) continue;

            ChatMessage lastMsg = chatMessageMapper.selectList(
                    new LambdaQueryWrapper<ChatMessage>()
                            .and(w -> w.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, otherId))
                            .or(w -> w.eq(ChatMessage::getSenderId, otherId).eq(ChatMessage::getReceiverId, userId))
                            .orderByDesc(ChatMessage::getCreateTime).last("limit 1")
            ).stream().findFirst().orElse(null);

            Long unreadCount = chatMessageMapper.selectCount(
                    new LambdaQueryWrapper<ChatMessage>()
                            .eq(ChatMessage::getSenderId, otherId)
                            .eq(ChatMessage::getReceiverId, userId)
                            .eq(ChatMessage::getIsRead, 0));

            conversations.add(ConversationResponse.builder()
                    .otherUserId(other.getId())
                    .otherUserName(other.getRealName() != null ? other.getRealName() : other.getUsername())
                    .otherUserAvatar(other.getAvatar()).otherUserRole(other.getRole())
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "")
                    .lastMessageTime(lastMsg != null ? lastMsg.getCreateTime() : null)
                    .unreadCount(unreadCount).build());
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
        Page<ChatMessage> result = chatMessageMapper.selectPage(page,
                new LambdaQueryWrapper<ChatMessage>()
                        .and(w -> w.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, otherUserId))
                        .or(w -> w.eq(ChatMessage::getSenderId, otherUserId).eq(ChatMessage::getReceiverId, userId))
                        .orderByDesc(ChatMessage::getCreateTime));

        User u1 = userMapper.selectById(userId);
        User u2 = userMapper.selectById(otherUserId);
        for (ChatMessage m : result.getRecords()) {
            User sender = m.getSenderId().equals(userId) ? u1 : u2;
            if (sender != null) {
                m.setSenderName(sender.getRealName() != null ? sender.getRealName() : sender.getUsername());
                m.setSenderAvatar(sender.getAvatar());
            }
        }
        return result;
    }

    public Long getUnreadCount(Long userId) {
        return chatMessageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getReceiverId, userId).eq(ChatMessage::getIsRead, 0));
    }

    public void markAsRead(Long userId, Long otherUserId) {
        chatMessageMapper.update(null,
                new LambdaUpdateWrapper<ChatMessage>()
                        .eq(ChatMessage::getSenderId, otherUserId)
                        .eq(ChatMessage::getReceiverId, userId)
                        .eq(ChatMessage::getIsRead, 0).set(ChatMessage::getIsRead, 1));
    }

    public List<User> searchUsers(Long currentUserId, String keyword, String role) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        w.ne(User::getId, currentUserId);
        w.and(wr -> wr.like(User::getUsername, keyword).or().like(User::getRealName, keyword));
        if (role != null && !role.isEmpty()) w.eq(User::getRole, role);
        w.last("limit 20");
        List<User> users = userMapper.selectList(w);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    /** 获取"可能认识的人"：同班同学、班主任、授课老师 */
    public List<User> getSuggestedContacts(Long currentUserId) {
        User me = userMapper.selectById(currentUserId);
        if (me == null) return Collections.emptyList();

        Set<Long> userIds = new LinkedHashSet<>();

        if ("student".equals(me.getRole())) {
            // 学生：同班同学 + 班级老师
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, currentUserId));
            if (student != null && student.getClassId() != null) {
                // 同班同学
                List<Student> classmates = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getClassId, student.getClassId())
                                .ne(Student::getUserId, currentUserId));
                for (Student s : classmates) {
                    if (s.getUserId() != null) userIds.add(s.getUserId());
                }
                // 班主任
                ClassInfo classInfo = classMapper.selectById(student.getClassId());
                if (classInfo != null && classInfo.getTeacher() != null) {
                    List<User> teachers = userMapper.selectList(
                            new LambdaQueryWrapper<User>()
                                    .eq(User::getRealName, classInfo.getTeacher())
                                    .eq(User::getRole, "teacher"));
                    for (User t : teachers) userIds.add(t.getId());
                }
            }
        } else if ("teacher".equals(me.getRole())) {
            // 教师：所教班级的学生 + 其他教师
            List<ClassInfo> myClasses = classMapper.selectList(
                    new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getTeacher, me.getRealName()));
            for (ClassInfo c : myClasses) {
                List<Student> classStudents = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>().eq(Student::getClassId, c.getId()));
                for (Student s : classStudents) {
                    if (s.getUserId() != null) userIds.add(s.getUserId());
                }
            }
            // 其他教师 + 管理员
            List<User> colleagues = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .ne(User::getId, currentUserId)
                            .in(User::getRole, "teacher", "admin"));
            for (User u : colleagues) userIds.add(u.getId());
        } else if ("admin".equals(me.getRole())) {
            // 管理员：所有教师
            List<User> teachers = userMapper.selectList(
                    new LambdaQueryWrapper<User>()
                            .ne(User::getId, currentUserId)
                            .eq(User::getRole, "teacher"));
            for (User t : teachers) userIds.add(t.getId());
        }

        if (userIds.isEmpty()) return Collections.emptyList();

        List<User> users = userMapper.selectBatchIds(userIds);
        users.forEach(u -> u.setPassword(null));

        // 按角色排序：teacher/admin 优先，然后 student
        users.sort((a, b) -> {
            int rankA = "student".equals(a.getRole()) ? 1 : 0;
            int rankB = "student".equals(b.getRole()) ? 1 : 0;
            return Integer.compare(rankA, rankB);
        });
        return users;
    }
}
