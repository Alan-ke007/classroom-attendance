package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生数据访问接口
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
