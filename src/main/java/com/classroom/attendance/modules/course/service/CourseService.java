package com.classroom.attendance.modules.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;

    public Page<Course> getCourseList(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        if ("teacher".equals(SecurityUtil.getCurrentRole())) {
            w.eq(Course::getTeacherId, SecurityUtil.getCurrentUserId());
        }
        w.orderByDesc(Course::getCreateTime);
        return courseMapper.selectPage(new Page<>(pageNum, pageSize), w);
    }

    public List<Course> getAllCourses() {
        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        if ("teacher".equals(SecurityUtil.getCurrentRole())) {
            w.eq(Course::getTeacherId, SecurityUtil.getCurrentUserId());
        }
        w.orderByAsc(Course::getCourseName);
        return courseMapper.selectList(w);
    }

    public List<Course> getByClassId(Long classId) {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getClassId, classId).orderByAsc(Course::getStartTime));
    }

    public Course getById(Long id) {
        Course c = courseMapper.selectById(id);
        BusinessException.notNull(c, "课程不存在");
        return c;
    }

    public Course create(Course course) {
        courseMapper.insert(course);
        return course;
    }

    public Course update(Long id, Course course) {
        BusinessException.notNull(courseMapper.selectById(id), "课程不存在");
        course.setId(id);
        courseMapper.updateById(course);
        return course;
    }

    public void delete(Long id) {
        BusinessException.isTrue(courseMapper.deleteById(id) > 0, "课程不存在或删除失败");
    }
}
