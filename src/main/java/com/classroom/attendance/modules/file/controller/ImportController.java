package com.classroom.attendance.modules.file.controller;

import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController extends BaseController {

    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;
    private final DataFormatter dataFormatter = new DataFormatter();

    @RequireRole({"admin", "teacher"})
    @PostMapping("/students")
    public Result<Map<String, Object>> importStudents(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            int success = 0, fail = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String studentNo = getCellString(row, 0);
                    String name = getCellString(row, 1);
                    String gender = getCellString(row, 2);
                    String className = getCellString(row, 3);
                    String phone = getCellString(row, 4);

                    if (studentNo == null || studentNo.isEmpty() || name == null || name.isEmpty()) {
                        fail++; errors.add("第" + (i + 1) + "行：学号或姓名为空"); continue;
                    }

                    Long classId = null;
                    if (className != null && !className.isEmpty()) {
                        ClassInfo ci = classMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassInfo>()
                                        .eq(ClassInfo::getClassName, className));
                        if (ci != null) classId = ci.getId();
                    }

                    studentMapper.insert(Student.builder()
                            .studentNo(studentNo).name(name).gender(gender)
                            .classId(classId).phone(phone).build());
                    success++;
                } catch (Exception e) {
                    fail++; errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
            workbook.close();

            return Result.success(Map.of("success", success, "fail", fail,
                    "total", success + fail, "errors", errors));
        } catch (Exception e) {
            throw new com.classroom.attendance.infrastructure.exception.BusinessException("导入失败: " + e.getMessage());
        }
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping("/courses")
    public Result<Map<String, Object>> importCourses(@RequestParam("file") MultipartFile file) {
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            int success = 0, fail = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    String courseName = getCellString(row, 0);
                    String classroom = getCellString(row, 1);
                    String startTimeStr = getCellString(row, 2);
                    String endTimeStr = getCellString(row, 3);
                    String weekDay = getCellString(row, 4);
                    String className = getCellString(row, 5);

                    if (courseName == null || courseName.isEmpty()) {
                        fail++; errors.add("第" + (i + 1) + "行：课程名称为空"); continue;
                    }

                    Long classId = null;
                    if (className != null && !className.isEmpty()) {
                        ClassInfo ci = classMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassInfo>()
                                        .eq(ClassInfo::getClassName, className));
                        if (ci != null) classId = ci.getId();
                    }

                    courseMapper.insert(Course.builder()
                            .courseName(courseName).classroom(classroom)
                            .startTime(startTimeStr != null ? LocalTime.parse(startTimeStr) : null)
                            .endTime(endTimeStr != null ? LocalTime.parse(endTimeStr) : null)
                            .weekDay(weekDay).classId(classId).build());
                    success++;
                } catch (Exception e) {
                    fail++; errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }
            workbook.close();

            return Result.success(Map.of("success", success, "fail", fail,
                    "total", success + fail, "errors", errors));
        } catch (Exception e) {
            throw new com.classroom.attendance.infrastructure.exception.BusinessException("导入失败: " + e.getMessage());
        }
    }

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = dataFormatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }
}
