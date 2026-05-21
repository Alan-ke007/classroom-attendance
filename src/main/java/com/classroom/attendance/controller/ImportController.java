package com.classroom.attendance.controller;

import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.mapper.ClassMapper;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.model.ClassInfo;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/import")
public class ImportController {

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 批量导入学生（Excel）
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping("/students")
    public Result<Map<String, Object>> importStudents(@RequestParam("file") MultipartFile file) {
        log.info("批量导入学生，文件名: {}", file.getOriginalFilename());
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
                        fail++;
                        errors.add("第" + (i + 1) + "行：学号或姓名为空");
                        continue;
                    }

                    // 查找班级ID
                    Long classId = null;
                    if (className != null && !className.isEmpty()) {
                        ClassInfo ci = classMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassInfo>()
                                        .eq(ClassInfo::getClassName, className)
                        );
                        if (ci != null) classId = ci.getId();
                    }

                    Student student = Student.builder()
                            .studentNo(studentNo)
                            .name(name)
                            .gender(gender)
                            .classId(classId)
                            .phone(phone)
                            .build();

                    studentMapper.insert(student);
                    success++;
                } catch (Exception e) {
                    fail++;
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }

            workbook.close();

            Map<String, Object> result = Map.of(
                    "success", success,
                    "fail", fail,
                    "total", success + fail,
                    "errors", errors
            );
            log.info("导入完成: 成功{}条, 失败{}条", success, fail);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入学生失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 批量导入课程（Excel）
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping("/courses")
    public Result<Map<String, Object>> importCourses(@RequestParam("file") MultipartFile file) {
        log.info("批量导入课程，文件名: {}", file.getOriginalFilename());
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
                        fail++;
                        errors.add("第" + (i + 1) + "行：课程名称为空");
                        continue;
                    }

                    Long classId = null;
                    if (className != null && !className.isEmpty()) {
                        ClassInfo ci = classMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassInfo>()
                                        .eq(ClassInfo::getClassName, className)
                        );
                        if (ci != null) classId = ci.getId();
                    }

                    Course course = Course.builder()
                            .courseName(courseName)
                            .classroom(classroom)
                            .startTime(startTimeStr != null ? LocalTime.parse(startTimeStr) : null)
                            .endTime(endTimeStr != null ? LocalTime.parse(endTimeStr) : null)
                            .weekDay(weekDay)
                            .classId(classId)
                            .build();

                    courseMapper.insert(course);
                    success++;
                } catch (Exception e) {
                    fail++;
                    errors.add("第" + (i + 1) + "行：" + e.getMessage());
                }
            }

            workbook.close();

            Map<String, Object> result = Map.of(
                    "success", success,
                    "fail", fail,
                    "total", success + fail,
                    "errors", errors
            );
            log.info("导入完成: 成功{}条, 失败{}条", success, fail);
            return Result.success(result);
        } catch (Exception e) {
            log.error("导入课程失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    private final DataFormatter dataFormatter = new DataFormatter();

    private String getCellString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return null;
        String value = dataFormatter.formatCellValue(cell);
        return value != null ? value.trim() : null;
    }
}
