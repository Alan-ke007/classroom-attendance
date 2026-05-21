package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.model.ClassInfo;
import com.classroom.attendance.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/class")
public class ClassController {
    
    @Autowired
    private ClassService classService;
    
    /**
     * 分页查询班级列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<Page<ClassInfo>> getClassList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("接收到分页查询班级请求，页码: {}, 每页大小: {}", pageNum, pageSize);
        
        try {
            Page<ClassInfo> page = classService.getClassList(pageNum, pageSize);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询班级列表失败", e);
            return Result.error("查询班级列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有班级
     * @return 班级列表
     */
    @GetMapping("/all")
    public Result<List<ClassInfo>> getAllClasses() {
        log.info("接收到查询所有班级请求");
        
        try {
            List<ClassInfo> classes = classService.getAllClasses();
            return Result.success(classes);
        } catch (Exception e) {
            log.error("查询所有班级失败", e);
            return Result.error("查询所有班级失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询班级
     * @param id 班级ID
     * @return 班级信息
     */
    @GetMapping("/{id}")
    public Result<ClassInfo> getClassById(@PathVariable Long id) {
        log.info("接收到查询班级详情请求，ID: {}", id);
        
        try {
            ClassInfo classInfo = classService.getClassById(id);
            if (classInfo == null) {
                return Result.fail("班级不存在");
            }
            return Result.success(classInfo);
        } catch (Exception e) {
            log.error("查询班级详情失败，ID: {}", id, e);
            return Result.error("查询班级详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加班级
     * @param classInfo 班级信息
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<Void> addClass(@RequestBody ClassInfo classInfo) {
        log.info("接收到添加班级请求: {}", classInfo.getClassName());
        
        try {
            // 参数校验
            if (classInfo.getClassName() == null || classInfo.getClassName().trim().isEmpty()) {
                return Result.fail("班级名称不能为空");
            }
            
            boolean success = classService.addClass(classInfo);
            if (success) {
                return Result.success("添加班级成功", null);
            } else {
                return Result.fail("班级名称已存在");
            }
        } catch (Exception e) {
            log.error("添加班级失败", e);
            return Result.error("添加班级失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新班级
     * @param id 班级ID
     * @param classInfo 班级信息
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<Void> updateClass(@PathVariable Long id, @RequestBody ClassInfo classInfo) {
        log.info("接收到更新班级请求，ID: {}", id);
        
        try {
            // 参数校验
            if (classInfo.getClassName() == null || classInfo.getClassName().trim().isEmpty()) {
                return Result.fail("班级名称不能为空");
            }
            
            classInfo.setId(id);
            boolean success = classService.updateClass(classInfo);
            if (success) {
                return Result.success("更新班级成功", null);
            } else {
                return Result.fail("班级不存在");
            }
        } catch (Exception e) {
            log.error("更新班级失败，ID: {}", id, e);
            return Result.error("更新班级失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除班级
     * @param id 班级ID
     * @return 操作结果
     */
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<Void> deleteClass(@PathVariable Long id) {
        log.info("接收到删除班级请求，ID: {}", id);
        
        try {
            boolean success = classService.deleteClass(id);
            if (success) {
                return Result.success("删除班级成功", null);
            } else {
                return Result.fail("班级不存在");
            }
        } catch (Exception e) {
            log.error("删除班级失败，ID: {}", id, e);
            return Result.error("删除班级失败: " + e.getMessage());
        }
    }
}
