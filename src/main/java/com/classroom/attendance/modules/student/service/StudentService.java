package com.classroom.attendance.modules.student.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;

    public Page<Student> getStudentList(Integer pageNum, Integer pageSize) {
        Page<Student> page = new Page<>(pageNum, pageSize);
        Page<Student> result = studentMapper.selectPage(page,
                new LambdaQueryWrapper<Student>().orderByDesc(Student::getCreateTime));
        fillClassNames(result.getRecords());
        return result;
    }

    public Page<Student> listForCurrentUser(Integer pageNum, Integer pageSize) {
        List<Long> classIds = getTeacherClassIds();
        if (classIds != null && classIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }
        Page<Student> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Student> w = new LambdaQueryWrapper<>();
        if (classIds != null) w.in(Student::getClassId, classIds);
        w.orderByDesc(Student::getCreateTime);
        Page<Student> result = studentMapper.selectPage(page, w);
        fillClassNames(result.getRecords());
        return result;
    }

    public List<Student> getAllStudents() {
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>().orderByDesc(Student::getCreateTime));
        fillClassNames(students);
        return students;
    }

    public List<Student> getByClassId(Long classId) {
        return studentMapper.selectList(
                new LambdaQueryWrapper<Student>().eq(Student::getClassId, classId));
    }

    public Student getById(Long id) {
        Student s = studentMapper.selectById(id);
        BusinessException.notNull(s, "学生不存在");
        if (s.getClassId() != null) {
            var ci = classMapper.selectById(s.getClassId());
            if (ci != null) s.setClassName(ci.getClassName());
        }
        return s;
    }

    public Student create(Student student) {
        BusinessException.isTrue(studentMapper.selectOne(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, student.getStudentNo())) == null,
                "学号已存在");
        studentMapper.insert(student);
        return student;
    }

    public Student update(Long id, Student student) {
        BusinessException.notNull(studentMapper.selectById(id), "学生不存在");
        student.setId(id);
        studentMapper.updateById(student);
        return student;
    }

    public void delete(Long id) {
        BusinessException.isTrue(studentMapper.deleteById(id) > 0, "学生不存在或删除失败");
    }

    public Student getByUserId(Long userId) {
        return studentMapper.selectOne(new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
    }

    private List<Long> getTeacherClassIds() {
        String role = SecurityUtil.getCurrentRole();
        if (!"teacher".equals(role)) return null;
        return classMapper.selectList(
                new LambdaQueryWrapper<com.classroom.attendance.modules.classmgmt.entity.ClassInfo>()
                        .eq(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getTeacher, SecurityUtil.getCurrentUserRealName()))
                .stream().map(com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getId).toList();
    }

    private void fillClassNames(List<Student> students) {
        if (students == null || students.isEmpty()) return;
        var classIds = students.stream().map(Student::getClassId).filter(id -> id != null).distinct().toList();
        if (classIds.isEmpty()) return;
        var classMap = classMapper.selectBatchIds(classIds).stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getId,
                        com.classroom.attendance.modules.classmgmt.entity.ClassInfo::getClassName));
        students.forEach(s -> {
            if (s.getClassId() != null && classMap.containsKey(s.getClassId())) {
                s.setClassName(classMap.get(s.getClassId()));
            }
        });
    }
}
