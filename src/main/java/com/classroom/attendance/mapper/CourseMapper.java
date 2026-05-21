package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程数据访问接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
