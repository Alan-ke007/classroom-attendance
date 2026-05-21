package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.Attendance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤记录数据访问接口
 */
@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
}
