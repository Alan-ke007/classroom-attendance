package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.ClassMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.model.ClassInfo;
import com.classroom.attendance.model.Student;
import com.classroom.attendance.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 */
@Slf4j
@Service
public class StudentServiceImpl implements StudentService {
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    @Override
    public Page<Student> getStudentList(Integer pageNum, Integer pageSize) {
        log.info("接收到分页查询学生请求，页码: {}, 每页大小: {}", pageNum, pageSize);
        
        Page<Student> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Student::getCreateTime);
        
        Page<Student> resultPage = studentMapper.selectPage(page, queryWrapper);
        
        // 填充班级名称
        fillClassName(resultPage.getRecords());
        
        return resultPage;
    }
    
    @Override
    public Student getStudentById(Long id) {
        log.info("查询学生详情，ID: {}", id);
        return studentMapper.selectById(id);
    }
    
    @Override
    public boolean addStudent(Student student) {
        log.info("添加学生: {}", student.getStudentNo());
        
        // 检查学号是否已存在
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentNo, student.getStudentNo());
        Long count = studentMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            log.warn("学号已存在: {}", student.getStudentNo());
            return false;
        }
        
        int result = studentMapper.insert(student);
        return result > 0;
    }
    
    @Override
    public boolean updateStudent(Student student) {
        log.info("更新学生: {}", student.getId());
        
        // 检查学生是否存在
        Student existingStudent = studentMapper.selectById(student.getId());
        if (existingStudent == null) {
            log.warn("学生不存在，ID: {}", student.getId());
            return false;
        }
        
        int result = studentMapper.updateById(student);
        return result > 0;
    }
    
    @Override
    public boolean deleteStudent(Long id) {
        log.info("删除学生，ID: {}", id);
        
        // 检查学生是否存在
        Student existingStudent = studentMapper.selectById(id);
        if (existingStudent == null) {
            log.warn("学生不存在，ID: {}", id);
            return false;
        }
        
        int result = studentMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public List<Student> getAllStudents() {
        log.info("查询所有学生");
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Student::getCreateTime);
        List<Student> students = studentMapper.selectList(queryWrapper);
        
        // 填充班级名称
        fillClassName(students);
        
        return students;
    }
    
    @Override
    public Student getStudentByUserId(Long userId) {
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getUserId, userId);
        return studentMapper.selectOne(queryWrapper);
    }

    /**
     * 填充班级名称
     */
    private void fillClassName(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return;
        }
        
        // 获取所有唯一的 classId
        List<Long> classIds = students.stream()
                .map(Student::getClassId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (classIds.isEmpty()) {
            return;
        }
        
        // 批量查询班级信息
        List<ClassInfo> classes = classMapper.selectBatchIds(classIds);
        Map<Long, String> classMap = classes.stream()
                .collect(Collectors.toMap(ClassInfo::getId, ClassInfo::getClassName));
        
        // 填充班级名称
        students.forEach(student -> {
            if (student.getClassId() != null && classMap.containsKey(student.getClassId())) {
                student.setClassName(classMap.get(student.getClassId()));
            }
        });
    }
}
