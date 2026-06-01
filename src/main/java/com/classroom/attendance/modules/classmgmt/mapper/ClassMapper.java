package com.classroom.attendance.modules.classmgmt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClassMapper extends BaseMapper<ClassInfo> {
}
