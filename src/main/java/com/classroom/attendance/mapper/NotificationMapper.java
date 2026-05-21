package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
