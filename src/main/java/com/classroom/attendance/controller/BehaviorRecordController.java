package com.classroom.attendance.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.annotation.RequireRole;
import com.classroom.attendance.common.Result;
import com.classroom.attendance.mapper.ClassMapper;
import com.classroom.attendance.mapper.CourseMapper;
import com.classroom.attendance.mapper.StudentMapper;
import com.classroom.attendance.dto.BehaviorDetectionDTO;
import com.classroom.attendance.model.BehaviorRecord;
import com.classroom.attendance.model.ClassInfo;
import com.classroom.attendance.model.Course;
import com.classroom.attendance.model.Student;
import com.classroom.attendance.service.BehaviorRecordService;
import com.classroom.attendance.utils.ExcelExportUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 行为识别记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/behavior")
public class BehaviorRecordController {

    @Autowired
    private BehaviorRecordService behaviorRecordService;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private ClassMapper classMapper;

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 分页查询行为记录列表（按角色过滤）
     */
    @GetMapping("/list")
    public Result<Page<BehaviorRecord>> getBehaviorList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        log.info("接收到分页查询行为记录请求");

        try {
            String role = (String) request.getAttribute("role");
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr == null) throw new RuntimeException("无法获取用户身份信息");
            Long userId = userIdAttr instanceof Long ? (Long) userIdAttr : ((Number) userIdAttr).longValue();
            Long filterStudentId = null;
            List<Long> filterClassIds = null;

            if ("student".equals(role)) {
                Student student = studentMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, userId));
                if (student != null) {
                    filterStudentId = student.getId();
                }
            } else if ("teacher".equals(role)) {
                List<Course> teacherCourses = courseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, userId)
                        .select(Course::getClassId));
                filterClassIds = teacherCourses.stream()
                    .map(Course::getClassId)
                    .distinct()
                    .collect(Collectors.toList());
            }

            Page<BehaviorRecord> page = behaviorRecordService.getBehaviorList(pageNum, pageSize, filterStudentId, filterClassIds);

            // 填充学生姓名和班级名称
            List<BehaviorRecord> records = page.getRecords();
            if (!records.isEmpty()) {
                Map<Long, Student> studentMap = new HashMap<>();
                Map<Long, ClassInfo> classMap = new HashMap<>();
                for (BehaviorRecord record : records) {
                    if (record.getStudentId() != null && !studentMap.containsKey(record.getStudentId())) {
                        Student s = studentMapper.selectById(record.getStudentId());
                        if (s != null) studentMap.put(record.getStudentId(), s);
                    }
                    if (record.getClassId() != null && !classMap.containsKey(record.getClassId())) {
                        ClassInfo c = classMapper.selectById(record.getClassId());
                        if (c != null) classMap.put(record.getClassId(), c);
                    }
                }
                for (BehaviorRecord record : records) {
                    Student s = studentMap.get(record.getStudentId());
                    ClassInfo ci = classMap.get(record.getClassId());
                    record.setStudentName(s != null ? s.getName() : "未知");
                    record.setClassName(ci != null ? ci.getClassName() : "未知");
                }
            }

            return Result.success(page);
        } catch (Exception e) {
            log.error("查询行为记录列表失败", e);
            return Result.error("查询行为记录列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询行为记录详情
     */
    @GetMapping("/{id}")
    public Result<BehaviorRecord> getBehaviorById(@PathVariable Long id) {
        log.info("接收到查询行为记录详情请求，ID: {}", id);
        
        try {
            BehaviorRecord behaviorRecord = behaviorRecordService.getBehaviorById(id);
            if (behaviorRecord != null) {
                return Result.success(behaviorRecord);
            } else {
                return Result.error("行为记录不存在");
            }
        } catch (Exception e) {
            log.error("查询行为记录详情失败", e);
            return Result.error("查询行为记录详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据学生ID查询行为记录
     */
    @GetMapping("/student/{studentId}")
    public Result<List<BehaviorRecord>> getBehaviorsByStudentId(@PathVariable Long studentId) {
        log.info("接收到根据学生ID查询行为记录请求");
        
        try {
            List<BehaviorRecord> behaviors = behaviorRecordService.getBehaviorsByStudentId(studentId);
            return Result.success(behaviors);
        } catch (Exception e) {
            log.error("查询学生行为记录失败", e);
            return Result.error("查询学生行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据班级ID查询行为记录
     */
    @GetMapping("/class/{classId}")
    public Result<List<BehaviorRecord>> getBehaviorsByClassId(@PathVariable Long classId) {
        log.info("接收到根据班级ID查询行为记录请求");
        
        try {
            List<BehaviorRecord> behaviors = behaviorRecordService.getBehaviorsByClassId(classId);
            return Result.success(behaviors);
        } catch (Exception e) {
            log.error("查询班级行为记录失败", e);
            return Result.error("查询班级行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据行为类型查询行为记录
     */
    @GetMapping("/type/{behaviorType}")
    public Result<List<BehaviorRecord>> getBehaviorsByType(@PathVariable String behaviorType) {
        log.info("接收到根据行为类型查询行为记录请求");
        
        try {
            List<BehaviorRecord> behaviors = behaviorRecordService.getBehaviorsByType(behaviorType);
            return Result.success(behaviors);
        } catch (Exception e) {
            log.error("查询行为类型记录失败", e);
            return Result.error("查询行为类型记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询未处理的行为记录
     */
    @GetMapping("/unhandled")
    public Result<List<BehaviorRecord>> getUnhandledBehaviors() {
        log.info("接收到查询未处理行为记录请求");
        
        try {
            List<BehaviorRecord> behaviors = behaviorRecordService.getUnhandledBehaviors();
            return Result.success(behaviors);
        } catch (Exception e) {
            log.error("查询未处理行为记录失败", e);
            return Result.error("查询未处理行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据时间范围查询行为记录
     */
    @GetMapping("/range")
    public Result<List<BehaviorRecord>> getBehaviorsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("接收到根据时间范围查询行为记录请求");
        
        try {
            List<BehaviorRecord> behaviors = behaviorRecordService.getBehaviorsByTimeRange(startTime, endTime);
            return Result.success(behaviors);
        } catch (Exception e) {
            log.error("查询时间范围行为记录失败", e);
            return Result.error("查询时间范围行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 标记行为记录为已处理
     */
    @RequireRole({"admin", "teacher"})
    @PutMapping("/handle/{id}")
    public Result<String> markAsHandled(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        log.info("接收到标记行为记录为已处理请求，ID: {}", id);

        try {
            String handleRemark = body != null ? body.get("handleRemark") : null;
            boolean success = behaviorRecordService.markAsHandled(id, handleRemark);
            if (success) {
                return Result.success("标记为已处理成功");
            } else {
                return Result.error("行为记录不存在或标记失败");
            }
        } catch (Exception e) {
            log.error("标记行为记录失败", e);
            return Result.error("标记行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存 AI 检测结果（自动入库）
     * BehaviorMonitor 页面检测到行为后自动调用此接口
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping("/detection/save")
    public Result<String> saveDetectionResults(@RequestBody List<BehaviorDetectionDTO> detections, HttpServletRequest request) {
        log.info("接收到 AI 行为检测结果，数量: {}", detections != null ? detections.size() : 0);

        try {
            if (detections == null || detections.isEmpty()) {
                return Result.success("无检测结果需要保存");
            }

            // 获取用户上下文
            String role = (String) request.getAttribute("role");
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr == null) throw new RuntimeException("无法获取用户身份信息");
            Long userId = userIdAttr instanceof Long ? (Long) userIdAttr : ((Number) userIdAttr).longValue();

            int savedCount = 0;
            for (BehaviorDetectionDTO dto : detections) {
                BehaviorRecord record = new BehaviorRecord();
                record.setBehaviorType(dto.getBehaviorType());
                record.setConfidence(dto.getConfidence() != null
                        ? java.math.BigDecimal.valueOf(dto.getConfidence()) : null);
                record.setBehaviorTime(LocalDateTime.now());
                record.setHandled(0);

                // 尝试推断班级（教师取所教班级，管理员取请求中带的班级）
                if (dto.getClassId() != null) {
                    record.setClassId(dto.getClassId());
                } else if ("teacher".equals(role)) {
                    // 教师默认取第一个所教班级
                    List<Course> teacherCourses = courseMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                            .eq(Course::getTeacherId, userId)
                            .select(Course::getClassId));
                    if (!teacherCourses.isEmpty()) {
                        record.setClassId(teacherCourses.get(0).getClassId());
                    }
                }

                if (dto.getCourseId() != null) {
                    record.setCourseId(dto.getCourseId());
                }

                behaviorRecordService.addBehavior(record);
                savedCount++;
            }

            log.info("AI 检测结果已入库，共保存 {} 条", savedCount);
            return Result.success("已保存 " + savedCount + " 条行为记录");
        } catch (Exception e) {
            log.error("保存检测结果失败", e);
            return Result.error("保存检测结果失败: " + e.getMessage());
        }
    }

    /**
     * 添加行为记录
     */
    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addBehavior(@RequestBody BehaviorRecord behaviorRecord) {
        log.info("接收到添加行为记录请求");
        
        try {
            boolean success = behaviorRecordService.addBehavior(behaviorRecord);
            if (success) {
                return Result.success("添加行为记录成功");
            } else {
                return Result.error("添加行为记录失败");
            }
        } catch (Exception e) {
            log.error("添加行为记录失败", e);
            return Result.error("添加行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新行为记录
     */
    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateBehavior(@PathVariable Long id, @RequestBody BehaviorRecord behaviorRecord) {
        log.info("接收到更新行为记录请求，ID: {}", id);
        
        try {
            behaviorRecord.setId(id);
            boolean success = behaviorRecordService.updateBehavior(behaviorRecord);
            if (success) {
                return Result.success("更新行为记录成功");
            } else {
                return Result.error("行为记录不存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新行为记录失败", e);
            return Result.error("更新行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除行为记录
     */
    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteBehavior(@PathVariable Long id) {
        log.info("接收到删除行为记录请求，ID: {}", id);
        
        try {
            boolean success = behaviorRecordService.deleteBehavior(id);
            if (success) {
                return Result.success("删除行为记录成功");
            } else {
                return Result.error("行为记录不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除行为记录失败", e);
            return Result.error("删除行为记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出行为记录为Excel
     */
    @RequireRole({"admin", "teacher"})
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBehaviors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("接收到导出行为记录请求");
        
        try {
            List<BehaviorRecord> behaviors;
            if (startTime != null && endTime != null) {
                behaviors = behaviorRecordService.getBehaviorsByTimeRange(startTime, endTime);
            } else {
                // 获取所有行为记录
                Page<BehaviorRecord> page = behaviorRecordService.getBehaviorList(1, 10000, null, null);
                behaviors = page.getRecords();
            }
            
            // 预加载关联数据
            Map<Long, Student> studentMap = new HashMap<>();
            Map<Long, ClassInfo> classMap = new HashMap<>();
            for (BehaviorRecord b : behaviors) {
                if (b.getStudentId() != null && !studentMap.containsKey(b.getStudentId())) {
                    Student s = studentMapper.selectById(b.getStudentId());
                    if (s != null) studentMap.put(b.getStudentId(), s);
                }
                if (b.getClassId() != null && !classMap.containsKey(b.getClassId())) {
                    ClassInfo c = classMapper.selectById(b.getClassId());
                    if (c != null) classMap.put(b.getClassId(), c);
                }
            }

            // 转换为Excel数据格式
            String[] headers = {"学生姓名", "班级", "行为类型", "行为时间", "置信度", "是否处理", "处理备注"};
            List<Map<String, Object>> data = behaviors.stream().map(behavior -> {
                Map<String, Object> row = new HashMap<>();
                Student s = studentMap.get(behavior.getStudentId());
                ClassInfo ci = classMap.get(behavior.getClassId());
                row.put("studentName", s != null ? s.getName() : "未知");
                row.put("className", ci != null ? ci.getClassName() : "未知");
                row.put("behaviorType", getBehaviorTypeText(behavior.getBehaviorType()));
                row.put("behaviorTime", behavior.getBehaviorTime());
                row.put("confidence", behavior.getConfidence());
                row.put("handled", behavior.getHandled() == 1 ? "已处理" : "未处理");
                row.put("handleRemark", behavior.getHandleRemark());
                return row;
            }).collect(Collectors.toList());
            
            // 生成Excel文件
            byte[] excelData = ExcelExportUtil.exportExcel(headers, data, "行为记录");
            
            // 设置响应头
            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = URLEncoder.encode("行为记录_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentDispositionFormData("attachment", fileName);
            
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .body(excelData);
        } catch (IOException e) {
            log.error("导出行为记录失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取行为类型文本
     */
    private String getBehaviorTypeText(String behaviorType) {
        return switch (behaviorType.toLowerCase()) {
            case "sleeping" -> "睡觉";
            case "phone" -> "玩手机";
            case "eating" -> "吃东西";
            case "talking" -> "讲话";
            case "absent" -> "缺勤";
            default -> behaviorType;
        };
    }
}
