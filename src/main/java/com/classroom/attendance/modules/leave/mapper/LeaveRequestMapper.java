package com.classroom.attendance.modules.leave.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.modules.leave.entity.LeaveRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {
}
