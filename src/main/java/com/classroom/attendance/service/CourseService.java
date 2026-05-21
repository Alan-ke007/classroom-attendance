package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.Course;

import java.util.List;

/**
 * 课程服务接口
 */
public interface CourseService {
    
    /**
     * 分页查询课程列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Course> getCourseList(Integer pageNum, Integer pageSize);
    
    /**
     * 根据ID查询课程
     * @param id 课程ID
     * @return 课程信息
     */
    Course getCourseById(Long id);
    
    /**
     * 添加课程
     * @param course 课程信息
     * @return 是否成功
     */
    boolean addCourse(Course course);
    
    /**
     * 更新课程
     * @param course 课程信息
     * @return 是否成功
     */
    boolean updateCourse(Course course);
    
    /**
     * 删除课程
     * @param id 课程ID
     * @return 是否成功
     */
    boolean deleteCourse(Long id);
    
    /**
     * 查询所有课程
     * @return 课程列表
     */
    List<Course> getAllCourses();
    
    /**
     * 根据班级ID查询课程
     * @param classId 班级ID
     * @return 课程列表
     */
    List<Course> getCoursesByClassId(Long classId);
}
