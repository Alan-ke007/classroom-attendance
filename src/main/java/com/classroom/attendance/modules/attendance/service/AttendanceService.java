package com.classroom.attendance.modules.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.enums.AttendanceStatus;
import com.classroom.attendance.modules.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceMapper attendanceMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;

    public Page<Attendance> getAttendanceList(Integer pageNum, Integer pageSize, Long studentId,
                                               Collection<Long> classIds, String status,
                                               LocalDate startDate, LocalDate endDate) {
        Page<Attendance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Attendance> w = new LambdaQueryWrapper<>();
        if (studentId != null) w.eq(Attendance::getStudentId, studentId);
        if (!CollectionUtils.isEmpty(classIds)) w.in(Attendance::getClassId, classIds);
        if (status != null && !status.isEmpty()) w.eq(Attendance::getStatus, status);
        if (startDate != null) w.ge(Attendance::getAttendanceDate, startDate);
        if (endDate != null) w.le(Attendance::getAttendanceDate, endDate);
        w.orderByDesc(Attendance::getAttendanceDate).orderByDesc(Attendance::getCreateTime);

        Page<Attendance> result = attendanceMapper.selectPage(page, w);
        fillRelatedData(result.getRecords());
        return result;
    }

    /**
     * F9 管理端人脸复核列表：按 faceStatus 筛选（默认 NEED_REVIEW），支持班级/课程/姓名收窄。
     * 仅 admin/teacher 可调（controller 已加 @RequireRole）。
     */
    public Page<Attendance> getFaceReviewList(Integer pageNum, Integer pageSize,
                                              String faceStatus, Long classId, Long courseId,
                                              String studentName) {
        String fs = (faceStatus == null || faceStatus.isEmpty()) ? "NEED_REVIEW" : faceStatus;
        Page<Attendance> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Attendance> w = new LambdaQueryWrapper<>();
        w.eq(Attendance::getFaceStatus, fs);
        if (classId != null) w.eq(Attendance::getClassId, classId);
        if (courseId != null) w.eq(Attendance::getCourseId, courseId);
        if (studentName != null && !studentName.isEmpty()) {
            List<Long> matched = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>().like(Student::getName, studentName).select(Student::getId))
                    .stream().map(Student::getId).toList();
            if (matched.isEmpty()) {
                Page<Attendance> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                return empty;
            }
            w.in(Attendance::getStudentId, matched);
        }
        w.orderByDesc(Attendance::getAttendanceDate).orderByDesc(Attendance::getCreateTime);
        Page<Attendance> result = attendanceMapper.selectPage(page, w);
        fillRelatedData(result.getRecords());
        return result;
    }

    public Page<Attendance> listForCurrentUser(Integer pageNum, Integer pageSize,
                                                String studentName, String status,
                                                LocalDate startDate, LocalDate endDate) {
        String role = SecurityUtil.getCurrentRole();
        Long userId = SecurityUtil.getCurrentUserId();

        Long filterStudentId = null;
        List<Long> filterClassIds = null;

        if ("student".equals(role)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>().eq(Student::getUserId, userId));
            if (student != null) filterStudentId = student.getId();
        } else if ("teacher".equals(role)) {
            filterClassIds = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, userId).select(Course::getClassId))
                    .stream().map(Course::getClassId).distinct().collect(Collectors.toList());
        }

        if (studentName != null && !studentName.isEmpty()) {
            List<Long> matchedIds = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>().like(Student::getName, studentName).select(Student::getId))
                    .stream().map(Student::getId).toList();
            if (matchedIds.isEmpty()) {
                Page<Attendance> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                return empty;
            }
            filterStudentId = matchedIds.get(0);
        }

        // 数据越权防护：scoped 角色（学生/教师）若解析不到任何过滤条件，
        // 绝不能回退到“全校数据”，否则会泄漏其他学生的考勤。仅 admin 可看全部。
        if (!"admin".equals(role) && filterStudentId == null && CollectionUtils.isEmpty(filterClassIds)) {
            Page<Attendance> empty = new Page<>(pageNum, pageSize);
            empty.setTotal(0);
            return empty;
        }

        return getAttendanceList(pageNum, pageSize, filterStudentId, filterClassIds, status, startDate, endDate);
    }

    public Attendance getById(Long id) {
        Attendance a = attendanceMapper.selectById(id);
        BusinessException.notNull(a, "考勤记录不存在");
        return a;
    }

    public Attendance create(Attendance attendance) {
        attendanceMapper.insert(attendance);
        return attendance;
    }

    public Attendance update(Long id, Attendance attendance) {
        BusinessException.notNull(attendanceMapper.selectById(id), "考勤记录不存在");
        attendance.setId(id);
        attendanceMapper.updateById(attendance);
        return attendance;
    }

    public void delete(Long id) {
        BusinessException.isTrue(attendanceMapper.deleteById(id) > 0, "考勤记录不存在或删除失败");
    }

    public List<Attendance> getByStudentId(Long studentId) {
        return attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>().eq(Attendance::getStudentId, studentId)
                        .orderByDesc(Attendance::getAttendanceDate));
    }

    public List<Attendance> getByClassId(Long classId) {
        return attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>().eq(Attendance::getClassId, classId)
                        .orderByDesc(Attendance::getAttendanceDate));
    }

    public List<Attendance> getByCourseId(Long courseId) {
        return attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>().eq(Attendance::getCourseId, courseId)
                        .orderByDesc(Attendance::getAttendanceDate));
    }

    public List<Attendance> getByDateRange(LocalDate start, LocalDate end) {
        return attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>().ge(Attendance::getAttendanceDate, start)
                        .le(Attendance::getAttendanceDate, end).orderByDesc(Attendance::getAttendanceDate));
    }

    public Double calculateRate(Long studentId, LocalDate start, LocalDate end) {
        List<Attendance> list = attendanceMapper.selectList(
                new LambdaQueryWrapper<Attendance>().eq(Attendance::getStudentId, studentId)
                        .ge(Attendance::getAttendanceDate, start).le(Attendance::getAttendanceDate, end));
        if (list.isEmpty()) return 0.0;
        long presentCount = list.stream().filter(a -> AttendanceStatus.PRESENT.equals(a.getStatus())).count();
        return (double) presentCount / list.size() * 100;
    }

    public List<Attendance> getExportData(LocalDate start, LocalDate end) {
        if (start != null && end != null) return getByDateRange(start, end);
        return getAttendanceList(1, 10000, null, null, null, null, null).getRecords();
    }

    public Map<String, Object> buildExportRow(Attendance a) {
        Student s = a.getStudentId() != null ? studentMapper.selectById(a.getStudentId()) : null;
        ClassInfo ci = a.getClassId() != null ? classMapper.selectById(a.getClassId()) : null;
        Course co = a.getCourseId() != null ? courseMapper.selectById(a.getCourseId()) : null;
        return Map.of(
                "studentNo", s != null ? s.getStudentNo() : "未知",
                "name", s != null ? s.getName() : "未知",
                "className", ci != null ? ci.getClassName() : "未知",
                "courseName", co != null ? co.getCourseName() : "未知",
                "attendanceDate", a.getAttendanceDate(),
                "status", a.getStatus(),
                "checkInTime", a.getCheckInTime(),
                "confidence", a.getConfidence(),
                "remark", a.getRemark() != null ? a.getRemark() : "");
    }

    private void fillRelatedData(List<Attendance> list) {
        if (CollectionUtils.isEmpty(list)) return;

        List<Long> studentIds = list.stream().map(Attendance::getStudentId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> courseIds = list.stream().map(Attendance::getCourseId).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, Student> studentMap = studentIds.isEmpty() ? Map.of() :
                studentMapper.selectBatchIds(studentIds).stream().collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, String> classNameMap = studentMap.values().stream()
                .map(Student::getClassId).filter(id -> id != null).distinct().collect(Collectors.toList()).isEmpty() ? Map.of() :
                classMapper.selectBatchIds(studentMap.values().stream().map(Student::getClassId).filter(id -> id != null).distinct().collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(ClassInfo::getId, ClassInfo::getClassName));
        Map<Long, String> courseNameMap = courseIds.isEmpty() ? Map.of() :
                courseMapper.selectBatchIds(courseIds).stream().collect(Collectors.toMap(Course::getId, Course::getCourseName));

        list.forEach(a -> {
            if (a.getStudentId() != null && studentMap.containsKey(a.getStudentId())) {
                Student s = studentMap.get(a.getStudentId());
                a.setStudentName(s.getName());
                if (s.getClassId() != null && classNameMap.containsKey(s.getClassId())) {
                    a.setClassName(classNameMap.get(s.getClassId()));
                }
            }
            if (a.getCourseId() != null && courseNameMap.containsKey(a.getCourseId())) {
                a.setCourseName(courseNameMap.get(a.getCourseId()));
            }
        });
    }
}
