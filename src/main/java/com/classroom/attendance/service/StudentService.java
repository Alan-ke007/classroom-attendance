package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.Student;

import java.util.List;

/**
 * 学生服务接口
 */
public interface StudentService {
    
    /**
     * 分页查询学生列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<Student> getStudentList(Integer pageNum, Integer pageSize);
    
    /**
     * 根据ID查询学生
     * @param id 学生ID
     * @return 学生信息
     */
    Student getStudentById(Long id);
    
    /**
     * 添加学生
     * @param student 学生信息
     * @return 是否成功
     */
    boolean addStudent(Student student);
    
    /**
     * 更新学生
     * @param student 学生信息
     * @return 是否成功
     */
    boolean updateStudent(Student student);
    
    /**
     * 删除学生
     * @param id 学生ID
     * @return 是否成功
     */
    boolean deleteStudent(Long id);
    
    /**
     * 查询所有学生
     * @return 学生列表
     */
    List<Student> getAllStudents();

    /**
     * 根据用户ID查询学生
     * @param userId 用户ID
     * @return 学生信息
     */
    Student getStudentByUserId(Long userId);
}
