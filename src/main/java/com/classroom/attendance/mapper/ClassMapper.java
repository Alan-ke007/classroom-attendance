package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.ClassInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级数据访问接口
 */
@Mapper
public interface ClassMapper extends BaseMapper<ClassInfo> {
}
