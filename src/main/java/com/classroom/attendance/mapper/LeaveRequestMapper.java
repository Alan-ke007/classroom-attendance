package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {
}
