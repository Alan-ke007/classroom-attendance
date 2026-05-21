package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.model.Student;
import com.classroom.attendance.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学生管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    /**
     * 分页查询学生列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<Student>> getStudentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("接收到分页查询学生请求，页码: {}, 每页大小: {}", pageNum, pageSize);
        
        try {
            Page<Student> page = studentService.getStudentList(pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询学生列表失败", e);
            return Result.error("查询学生列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有学生
     * @return 学生列表
     */
    @GetMapping("/all")
    public Result<List<Student>> getAllStudents() {
        log.info("接收到查询所有学生请求");
        
        try {
            List<Student> students = studentService.getAllStudents();
            return Result.success(students);
        } catch (Exception e) {
            log.error("查询所有学生失败", e);
            return Result.error("查询所有学生失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询学生详情
     * @param id 学生ID
     * @return 学生信息
     */
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Long id) {
        log.info("接收到查询学生详情请求，ID: {}", id);
        
        try {
            Student student = studentService.getStudentById(id);
            if (student != null) {
                return Result.success(student);
            } else {
                return Result.error("学生不存在");
            }
        } catch (Exception e) {
            log.error("查询学生详情失败", e);
            return Result.error("查询学生详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加学生
     * @param student 学生信息
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addStudent(@RequestBody Student student) {
        log.info("接收到添加学生请求: {}", student.getStudentNo());
        
        try {
            boolean success = studentService.addStudent(student);
            if (success) {
                return Result.success("添加学生成功");
            } else {
                return Result.error("学号已存在或添加失败");
            }
        } catch (Exception e) {
            log.error("添加学生失败", e);
            return Result.error("添加学生失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新学生
     * @param id 学生ID
     * @param student 学生信息
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        log.info("接收到更新学生请求，ID: {}", id);
        
        try {
            student.setId(id);
            boolean success = studentService.updateStudent(student);
            if (success) {
                return Result.success("更新学生成功");
            } else {
                return Result.error("学生不存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新学生失败", e);
            return Result.error("更新学生失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除学生
     * @param id 学生ID
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteStudent(@PathVariable Long id) {
        log.info("接收到删除学生请求，ID: {}", id);
        
        try {
            boolean success = studentService.deleteStudent(id);
            if (success) {
                return Result.success("删除学生成功");
            } else {
                return Result.error("学生不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除学生失败", e);
            return Result.error("删除学生失败: " + e.getMessage());
        }
    }
    
}

