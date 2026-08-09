package com.classroom.attendance.modules.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.modules.attendance.entity.Attendance;
import com.classroom.attendance.modules.attendance.mapper.AttendanceMapper;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceMapper attendanceMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private ClassMapper classMapper;
    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private AttendanceService service;

    // F9: 默认按 NEED_REVIEW 筛选人脸复核列表（不传 faceStatus 时）
    @Test
    void getFaceReviewList_defaultsToNeedReview() {
        when(attendanceMapper.selectPage(any(), any())).thenReturn(new Page<>());

        service.getFaceReviewList(1, 10, null, null, null, null);

        ArgumentCaptor<LambdaQueryWrapper<Attendance>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(attendanceMapper).selectPage(any(), captor.capture());
        // 过滤条件中应包含 face_status 维度（F9 复核筛选）
        assertTrue(captor.getValue().getCustomSqlSegment().contains("face_status"),
                "默认复核列表必须按 face_status 过滤");
    }

    // F9: 显式传入 faceStatus（如 VERIFIED）也应走同一过滤通道
    @Test
    void getFaceReviewList_explicitStatus() {
        when(attendanceMapper.selectPage(any(), any())).thenReturn(new Page<>());

        service.getFaceReviewList(1, 10, "VERIFIED", null, null, null);

        verify(attendanceMapper, times(1)).selectPage(any(), any());
    }

    // F9: 按学生姓名收窄时，应先据姓名查出 studentId 再 IN 过滤
    @Test
    void getFaceReviewList_narrowsByStudentName() {
        Student s = new Student();
        s.setId(5L);
        when(studentMapper.selectList(any())).thenReturn(List.of(s));
        when(attendanceMapper.selectPage(any(), any())).thenReturn(new Page<>());

        service.getFaceReviewList(1, 10, "Tom", null, null, "Tom");

        // 姓名收窄会先查学生表，再做 IN 过滤
        verify(studentMapper).selectList(any());
        verify(attendanceMapper).selectPage(any(), any());
    }
}
