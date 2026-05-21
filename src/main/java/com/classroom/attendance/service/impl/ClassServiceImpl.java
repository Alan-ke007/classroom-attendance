package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.ClassMapper;
import com.classroom.attendance.model.ClassInfo;
import com.classroom.attendance.service.ClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 班级服务实现类
 */
@Slf4j
@Service
public class ClassServiceImpl implements ClassService {
    
    @Autowired
    private ClassMapper classMapper;
    
    @Override
    public Page<ClassInfo> getClassList(Integer pageNum, Integer pageSize) {
        log.info("分页查询班级列表，页码: {}, 每页大小: {}", pageNum, pageSize);
        
        // 设置默认值
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        Page<ClassInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ClassInfo::getCreateTime);
        
        return classMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public ClassInfo getClassById(Long id) {
        log.info("根据ID查询班级，ID: {}", id);
        return classMapper.selectById(id);
    }
    
    @Override
    public boolean addClass(ClassInfo classInfo) {
        log.info("添加班级: {}", classInfo.getClassName());
        
        // 检查班级名称是否已存在
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClassInfo::getClassName, classInfo.getClassName());
        Long count = classMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            log.warn("班级名称已存在: {}", classInfo.getClassName());
            return false;
        }
        
        int result = classMapper.insert(classInfo);
        return result > 0;
    }
    
    @Override
    public boolean updateClass(ClassInfo classInfo) {
        log.info("更新班级: {}", classInfo.getId());
        
        // 检查班级是否存在
        ClassInfo existingClass = classMapper.selectById(classInfo.getId());
        if (existingClass == null) {
            log.warn("班级不存在，ID: {}", classInfo.getId());
            return false;
        }
        
        int result = classMapper.updateById(classInfo);
        return result > 0;
    }
    
    @Override
    public boolean deleteClass(Long id) {
        log.info("删除班级，ID: {}", id);
        
        // 检查班级是否存在
        ClassInfo existingClass = classMapper.selectById(id);
        if (existingClass == null) {
            log.warn("班级不存在，ID: {}", id);
            return false;
        }
        
        int result = classMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public List<ClassInfo> getAllClasses() {
        log.info("查询所有班级");
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ClassInfo::getCreateTime);
        return classMapper.selectList(queryWrapper);
    }
}
