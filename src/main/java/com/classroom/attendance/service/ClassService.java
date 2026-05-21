package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.ClassInfo;

import java.util.List;

/**
 * 班级服务接口
 */
public interface ClassService {
    
    /**
     * 分页查询班级列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<ClassInfo> getClassList(Integer pageNum, Integer pageSize);
    
    /**
     * 根据ID查询班级
     * @param id 班级ID
     * @return 班级信息
     */
    ClassInfo getClassById(Long id);
    
    /**
     * 添加班级
     * @param classInfo 班级信息
     * @return 是否成功
     */
    boolean addClass(ClassInfo classInfo);
    
    /**
     * 更新班级
     * @param classInfo 班级信息
     * @return 是否成功
     */
    boolean updateClass(ClassInfo classInfo);
    
    /**
     * 删除班级
     * @param id 班级ID
     * @return 是否成功
     */
    boolean deleteClass(Long id);
    
    /**
     * 查询所有班级
     * @return 班级列表
     */
    List<ClassInfo> getAllClasses();
}
