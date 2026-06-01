package com.classroom.attendance.modules.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.modules.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("""
        SELECT DISTINCT CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END
        FROM chat_message
        WHERE (sender_id = #{userId} OR receiver_id = #{userId}) AND deleted = 0
    """)
    List<Long> selectConversationUserIds(Long userId);
}
