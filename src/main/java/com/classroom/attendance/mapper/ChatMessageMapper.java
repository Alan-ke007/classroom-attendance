package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /** 获取与当前用户有过对话的所有用户ID */
    @Select("""
        SELECT DISTINCT CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END
        FROM chat_message
        WHERE (sender_id = #{userId} OR receiver_id = #{userId}) AND deleted = 0
    """)
    List<Long> selectConversationUserIds(Long userId);
}
