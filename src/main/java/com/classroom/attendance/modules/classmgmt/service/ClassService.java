package com.classroom.attendance.modules.classmgmt.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassMapper classMapper;

    private List<Long> getTeacherClassIds() {
        String role = SecurityUtil.getCurrentRole();
        if (!"teacher".equals(role)) return null;
        return classMapper.selectList(
                new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getTeacher, SecurityUtil.getCurrentUserRealName()))
                .stream().map(ClassInfo::getId).toList();
    }

    public Page<ClassInfo> getClassList(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<ClassInfo> w = new LambdaQueryWrapper<>();
        List<Long> classIds = getTeacherClassIds();
        if (classIds != null) {
            if (classIds.isEmpty()) return new Page<>(pageNum, pageSize);
            w.in(ClassInfo::getId, classIds);
        }
        w.orderByDesc(ClassInfo::getCreateTime);
        return classMapper.selectPage(new Page<>(pageNum, pageSize), w);
    }

    public List<ClassInfo> getAllClasses() {
        LambdaQueryWrapper<ClassInfo> w = new LambdaQueryWrapper<>();
        List<Long> classIds = getTeacherClassIds();
        if (classIds != null) {
            if (classIds.isEmpty()) return List.of();
            w.in(ClassInfo::getId, classIds);
        }
        w.orderByAsc(ClassInfo::getClassName);
        return classMapper.selectList(w);
    }

    public ClassInfo getById(Long id) {
        ClassInfo ci = classMapper.selectById(id);
        BusinessException.notNull(ci, "班级不存在");
        return ci;
    }

    public ClassInfo create(ClassInfo classInfo) {
        BusinessException.isTrue(classInfo.getClassName() != null && !classInfo.getClassName().trim().isEmpty(),
                "班级名称不能为空");
        BusinessException.isTrue(classMapper.selectOne(
                new LambdaQueryWrapper<ClassInfo>().eq(ClassInfo::getClassName, classInfo.getClassName())) == null,
                "班级名称已存在");
        classMapper.insert(classInfo);
        return classInfo;
    }

    public ClassInfo update(Long id, ClassInfo classInfo) {
        BusinessException.notNull(classMapper.selectById(id), "班级不存在");
        BusinessException.isTrue(classInfo.getClassName() != null && !classInfo.getClassName().trim().isEmpty(),
                "班级名称不能为空");
        classInfo.setId(id);
        classMapper.updateById(classInfo);
        return classInfo;
    }

    public void delete(Long id) {
        BusinessException.isTrue(classMapper.deleteById(id) > 0, "班级不存在");
    }
}
