package com.classroom.attendance.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController extends BaseController {

    private final CourseService courseService;

    @GetMapping("/list")
    public Result<Page<Course>> getCourseList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(courseService.getCourseList(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<Course>> getAllCourses() {
        return Result.success(courseService.getAllCourses());
    }

    @GetMapping("/class/{classId}")
    public Result<List<Course>> getCoursesByClassId(@PathVariable Long classId) {
        return Result.success(courseService.getByClassId(classId));
    }

    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addCourse(@RequestBody Course course) {
        courseService.create(course);
        return Result.success("添加课程成功");
    }

    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        courseService.update(id, course);
        return Result.success("更新课程成功");
    }

    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success("删除课程成功");
    }
}
