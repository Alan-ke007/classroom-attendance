package com.classroom.attendance.modules.student.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.constant.Constants;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.service.CreditScoreService;
import com.classroom.attendance.modules.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController extends BaseController {

    private final StudentService studentService;
    private final CreditScoreService creditScoreService;

    @GetMapping("/list")
    public Result<Page<Student>> getStudentList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(studentService.listForCurrentUser(pageNum, pageSize));
    }

    @RequireRole({"admin", "teacher"}) // H1：全量学生名单（PII）
    @GetMapping("/all")
    public Result<List<Student>> getAllStudents() {
        return Result.success(studentService.getAllStudents());
    }

    @RequireRole({"admin", "teacher"}) // H1：按班级查全量学生（PII）
    @GetMapping("/byClass")
    public Result<List<Student>> getByClassId(@RequestParam Long classId) {
        return Result.success(studentService.getByClassId(classId));
    }

    @RequireRole({"admin", "teacher"}) // H1：按 ID 查学生 PII
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addStudent(@RequestBody Student student) {
        studentService.create(student);
        return Result.success("添加学生成功");
    }

    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateStudent(@PathVariable Long id, @RequestBody Student student) {
        studentService.update(id, student);
        return Result.success("更新学生成功");
    }

    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
        return Result.success("删除学生成功");
    }

    // 归属裁剪：学生仅能查自己的学分；教师/管理员可查任意（H1 修复，避免学生端 403）
    @GetMapping("/credit-score/{id}")
    public Result<Map<String, Object>> getCreditScore(@PathVariable Long id) {
        if (Constants.Role.STUDENT.equals(SecurityUtil.getCurrentRole())
                && !id.equals(SecurityUtil.getCurrentStudentId())) {
            throw new BusinessException(403, "无权查看他人学分信息");
        }
        Student s = studentService.getById(id);
        int score = creditScoreService.getCreditScore(id);
        return Result.success(Map.of(
                "studentId", s.getId(),
                "studentName", s.getName(),
                "studentNo", s.getStudentNo(),
                "creditScore", score,
                "creditEarned", s.getCreditEarned() != null ? s.getCreditEarned() : 0,
                "creditDeducted", s.getCreditDeducted() != null ? s.getCreditDeducted() : 0
        ));
    }
}
