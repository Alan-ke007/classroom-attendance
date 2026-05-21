package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程服务实现类
 */
@Slf4j
@Service
public class CourseServiceImpl implements CourseService {
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Override
    public Page<Course> getCourseList(Integer pageNum, Integer pageSize) {
        log.info("接收到分页查询课程请求，页码: {}, 每页大小: {}", pageNum, pageSize);
        
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Course::getCreateTime);
        
        return courseMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public Course getCourseById(Long id) {
        log.info("查询课程详情，ID: {}", id);
        return courseMapper.selectById(id);
    }
    
    @Override
    public boolean addCourse(Course course) {
        log.info("添加课程: {}", course.getCourseName());
        
        int result = courseMapper.insert(course);
        return result > 0;
    }
    
    @Override
    public boolean updateCourse(Course course) {
        log.info("更新课程: {}", course.getId());
        
        // 检查课程是否存在
        Course existingCourse = courseMapper.selectById(course.getId());
        if (existingCourse == null) {
            log.warn("课程不存在，ID: {}", course.getId());
            return false;
        }
        
        int result = courseMapper.updateById(course);
        return result > 0;
    }
    
    @Override
    public boolean deleteCourse(Long id) {
        log.info("删除课程，ID: {}", id);
        
        // 检查课程是否存在
        Course existingCourse = courseMapper.selectById(id);
        if (existingCourse == null) {
            log.warn("课程不存在，ID: {}", id);
            return false;
        }
        
        int result = courseMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public List<Course> getAllCourses() {
        log.info("查询所有课程");
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Course::getCreateTime);
        return courseMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Course> getCoursesByClassId(Long classId) {
        log.info("根据班级ID查询课程，班级ID: {}", classId);
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Course::getClassId, classId);
        queryWrapper.orderByDesc(Course::getCreateTime);
        return courseMapper.selectList(queryWrapper);
    }
}
