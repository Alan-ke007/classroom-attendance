package com.classroom.attendance.modules.classmgmt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import com.classroom.attendance.modules.classmgmt.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController extends BaseController {

    private final ClassService classService;

    @GetMapping("/list")
    public Result<Page<ClassInfo>> getClassList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(classService.getClassList(pageNum, pageSize));
    }

    @GetMapping("/all")
    public Result<List<ClassInfo>> getAllClasses() {
        return Result.success(classService.getAllClasses());
    }

    @GetMapping("/{id}")
    public Result<ClassInfo> getClassById(@PathVariable Long id) {
        return Result.success(classService.getById(id));
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<Void> addClass(@RequestBody ClassInfo classInfo) {
        classService.create(classInfo);
        return Result.success("添加班级成功", null);
    }

    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<Void> updateClass(@PathVariable Long id, @RequestBody ClassInfo classInfo) {
        classService.update(id, classInfo);
        return Result.success("更新班级成功", null);
    }

    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<Void> deleteClass(@PathVariable Long id) {
        classService.delete(id);
        return Result.success("删除班级成功", null);
    }
}
