package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.mapper.ClassMapper;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.model.Attendance;
import com.classroom.attendance.model.Student;
import com.classroom.attendance.model.ClassInfo;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.service.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 考勤记录服务实现类
 */
@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {
    
    @Autowired
    private AttendanceMapper attendanceMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Override
    public Page<Attendance> getAttendanceList(Integer pageNum, Integer pageSize, Long studentId, Collection<Long> classIds) {
        log.info("分页查询考勤，页码: {}, 每页大小: {}, studentId: {}, classIds: {}", pageNum, pageSize, studentId, classIds);

        Page<Attendance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            queryWrapper.eq(Attendance::getStudentId, studentId);
        }
        if (!CollectionUtils.isEmpty(classIds)) {
            queryWrapper.in(Attendance::getClassId, classIds);
        }
        queryWrapper.orderByDesc(Attendance::getAttendanceDate);
        queryWrapper.orderByDesc(Attendance::getCreateTime);

        Page<Attendance> resultPage = attendanceMapper.selectPage(page, queryWrapper);
        
        // 填充关联数据（学生姓名、班级名称、课程名称）
        fillRelatedData(resultPage.getRecords());
        
        return resultPage;
    }
    
    @Override
    public Attendance getAttendanceById(Long id) {
        log.info("查询考勤记录详情，ID: {}", id);
        return attendanceMapper.selectById(id);
    }
    
    @Override
    public boolean addAttendance(Attendance attendance) {
        log.info("添加考勤记录: 学生ID={}, 课程ID={}", attendance.getStudentId(), attendance.getCourseId());
        
        int result = attendanceMapper.insert(attendance);
        return result > 0;
    }
    
    @Override
    public boolean updateAttendance(Attendance attendance) {
        log.info("更新考勤记录: {}", attendance.getId());
        
        // 检查记录是否存在
        Attendance existingAttendance = attendanceMapper.selectById(attendance.getId());
        if (existingAttendance == null) {
            log.warn("考勤记录不存在，ID: {}", attendance.getId());
            return false;
        }
        
        int result = attendanceMapper.updateById(attendance);
        return result > 0;
    }
    
    @Override
    public boolean deleteAttendance(Long id) {
        log.info("删除考勤记录，ID: {}", id);
        
        // 检查记录是否存在
        Attendance existingAttendance = attendanceMapper.selectById(id);
        if (existingAttendance == null) {
            log.warn("考勤记录不存在，ID: {}", id);
            return false;
        }
        
        int result = attendanceMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public List<Attendance> getAttendancesByStudentId(Long studentId) {
        log.info("根据学生ID查询考勤记录，学生ID: {}", studentId);
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attendance::getStudentId, studentId);
        queryWrapper.orderByDesc(Attendance::getAttendanceDate);
        return attendanceMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Attendance> getAttendancesByClassId(Long classId) {
        log.info("根据班级ID查询考勤记录，班级ID: {}", classId);
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attendance::getClassId, classId);
        queryWrapper.orderByDesc(Attendance::getAttendanceDate);
        return attendanceMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Attendance> getAttendancesByCourseId(Long courseId) {
        log.info("根据课程ID查询考勤记录，课程ID: {}", courseId);
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attendance::getCourseId, courseId);
        queryWrapper.orderByDesc(Attendance::getAttendanceDate);
        return attendanceMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<Attendance> getAttendancesByDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("根据日期范围查询考勤记录，{} 到 {}", startDate, endDate);
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Attendance::getAttendanceDate, startDate);
        queryWrapper.le(Attendance::getAttendanceDate, endDate);
        queryWrapper.orderByDesc(Attendance::getAttendanceDate);
        return attendanceMapper.selectList(queryWrapper);
    }
    
    @Override
    public Double calculateAttendanceRate(Long studentId, LocalDate startDate, LocalDate endDate) {
        log.info("计算学生出勤率，学生ID: {}, 日期范围: {} - {}", studentId, startDate, endDate);
        
        // 查询该时间段内的所有考勤记录
        LambdaQueryWrapper<Attendance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Attendance::getStudentId, studentId);
        queryWrapper.ge(Attendance::getAttendanceDate, startDate);
        queryWrapper.le(Attendance::getAttendanceDate, endDate);
        
        List<Attendance> attendances = attendanceMapper.selectList(queryWrapper);
        
        if (attendances.isEmpty()) {
            return 0.0;
        }
        
        // 统计出勤次数（status为present的记录）
        long presentCount = attendances.stream()
                .filter(a -> "present".equals(a.getStatus()))
                .count();
        
        // 计算出勤率
        double rate = (double) presentCount / attendances.size() * 100;
        
        log.info("学生ID: {} 的出勤率为: {}%", studentId, String.format("%.1f", rate));
        return rate;
    }
    
    /**
     * 填充关联数据（学生姓名、班级名称、课程名称）
     */
    private void fillRelatedData(List<Attendance> attendances) {
        if (attendances == null || attendances.isEmpty()) {
            return;
        }
        
        // 获取所有唯一的 studentId
        List<Long> studentIds = attendances.stream()
                .map(Attendance::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 获取所有唯一的 courseId
        List<Long> courseIds = attendances.stream()
                .map(Attendance::getCourseId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询学生信息
        Map<Long, Student> studentMap = studentIds.isEmpty() ? Map.of() :
                studentMapper.selectBatchIds(studentIds).stream()
                        .collect(Collectors.toMap(Student::getId, s -> s));
        
        // 批量查询班级信息
        List<Long> classIds = studentMap.values().stream()
                .map(Student::getClassId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> classNameMap = classIds.isEmpty() ? Map.of() :
                classMapper.selectBatchIds(classIds).stream()
                        .collect(Collectors.toMap(ClassInfo::getId, ClassInfo::getClassName));
        
        // 批量查询课程信息
        Map<Long, String> courseNameMap = courseIds.isEmpty() ? Map.of() :
                courseMapper.selectBatchIds(courseIds).stream()
                        .collect(Collectors.toMap(Course::getId, Course::getCourseName));
        
        // 填充关联数据
        attendances.forEach(attendance -> {
            if (attendance.getStudentId() != null && studentMap.containsKey(attendance.getStudentId())) {
                Student student = studentMap.get(attendance.getStudentId());
                attendance.setStudentName(student.getName());
                if (student.getClassId() != null && classNameMap.containsKey(student.getClassId())) {
                    attendance.setClassName(classNameMap.get(student.getClassId()));
                }
            }
            if (attendance.getCourseId() != null && courseNameMap.containsKey(attendance.getCourseId())) {
                attendance.setCourseName(courseNameMap.get(attendance.getCourseId()));
            }
        });
    }
}
