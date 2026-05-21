package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/course")
public class CourseController {
    
    @Autowired
    private CourseService courseService;
    
    /**
     * 分页查询课程列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<Course>> getCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("接收到分页查询课程请求，页码: {}, 每页大小: {}", pageNum, pageSize);
        
        try {
            Page<Course> page = courseService.getCourseList(pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询课程列表失败", e);
            return Result.error("查询课程列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有课程
     * @return 课程列表
     */
    @GetMapping("/all")
    public Result<List<Course>> getAllCourses() {
        log.info("接收到查询所有课程请求");
        
        try {
            List<Course> courses = courseService.getAllCourses();
            return Result.success(courses);
        } catch (Exception e) {
            log.error("查询所有课程失败", e);
            return Result.error("查询所有课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据班级ID查询课程
     * @param classId 班级ID
     * @return 课程列表
     */
    @GetMapping("/class/{classId}")
    public Result<List<Course>> getCoursesByClassId(@PathVariable Long classId) {
        log.info("接收到根据班级ID查询课程请求，班级ID: {}", classId);
        
        try {
            List<Course> courses = courseService.getCoursesByClassId(classId);
            return Result.success(courses);
        } catch (Exception e) {
            log.error("查询班级课程失败", e);
            return Result.error("查询班级课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询课程详情
     * @param id 课程ID
     * @return 课程信息
     */
    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable Long id) {
        log.info("接收到查询课程详情请求，ID: {}", id);
        
        try {
            Course course = courseService.getCourseById(id);
            if (course != null) {
                return Result.success(course);
            } else {
                return Result.error("课程不存在");
            }
        } catch (Exception e) {
            log.error("查询课程详情失败", e);
            return Result.error("查询课程详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加课程
     * @param course 课程信息
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addCourse(@RequestBody Course course) {
        log.info("接收到添加课程请求: {}", course.getCourseName());
        
        try {
            boolean success = courseService.addCourse(course);
            if (success) {
                return Result.success("添加课程成功");
            } else {
                return Result.error("添加课程失败");
            }
        } catch (Exception e) {
            log.error("添加课程失败", e);
            return Result.error("添加课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新课程
     * @param id 课程ID
     * @param course 课程信息
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        log.info("接收到更新课程请求，ID: {}", id);
        
        try {
            course.setId(id);
            boolean success = courseService.updateCourse(course);
            if (success) {
                return Result.success("更新课程成功");
            } else {
                return Result.error("课程不存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新课程失败", e);
            return Result.error("更新课程失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除课程
     * @param id 课程ID
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteCourse(@PathVariable Long id) {
        log.info("接收到删除课程请求，ID: {}", id);
        
        try {
            boolean success = courseService.deleteCourse(id);
            if (success) {
                return Result.success("删除课程成功");
            } else {
                return Result.error("课程不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除课程失败", e);
            return Result.error("删除课程失败: " + e.getMessage());
        }
    }
}
