package com.classroom.attendance.modules.behavior.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import com.classroom.attendance.infrastructure.base.BaseController;
import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.infrastructure.util.ExcelExportUtil;
import com.classroom.attendance.modules.behavior.dto.BehaviorDetectionDTO;
import com.classroom.attendance.modules.behavior.entity.BehaviorRecord;
import com.classroom.attendance.modules.behavior.service.BehaviorRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class BehaviorRecordController extends BaseController {

    private final BehaviorRecordService behaviorRecordService;

    @GetMapping("/list")
    public Result<Page<BehaviorRecord>> getBehaviorList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String behaviorType,
            @RequestParam(required = false) Integer handled) {
        return Result.success(behaviorRecordService.listForCurrentUser(pageNum, pageSize, studentName, behaviorType, handled));
    }

    @GetMapping("/{id}")
    public Result<BehaviorRecord> getBehaviorById(@PathVariable Long id) {
        return Result.success(behaviorRecordService.getById(id));
    }

    @GetMapping("/student/{studentId}")
    public Result<List<BehaviorRecord>> getBehaviorsByStudentId(@PathVariable Long studentId) {
        return Result.success(behaviorRecordService.getByStudentId(studentId));
    }

    @GetMapping("/class/{classId}")
    public Result<List<BehaviorRecord>> getBehaviorsByClassId(@PathVariable Long classId) {
        return Result.success(behaviorRecordService.getByClassId(classId));
    }

    @GetMapping("/type/{behaviorType}")
    public Result<List<BehaviorRecord>> getBehaviorsByType(@PathVariable String behaviorType) {
        return Result.success(behaviorRecordService.getByType(behaviorType));
    }

    @GetMapping("/unhandled")
    public Result<List<BehaviorRecord>> getUnhandledBehaviors() {
        return Result.success(behaviorRecordService.getUnhandled());
    }

    @GetMapping("/range")
    public Result<List<BehaviorRecord>> getBehaviorsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(behaviorRecordService.getByTimeRange(startTime, endTime));
    }

    @RequireRole({"admin", "teacher"})
    @PutMapping("/handle/{id}")
    public Result<String> markAsHandled(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String handleRemark = body != null ? body.get("handleRemark") : null;
        behaviorRecordService.markAsHandled(id, handleRemark);
        return Result.success("标记为已处理成功");
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping("/detection/save")
    public Result<String> saveDetectionResults(@RequestBody List<BehaviorDetectionDTO> detections) {
        if (detections == null || detections.isEmpty()) return Result.success("无检测结果需要保存");
        int savedCount = behaviorRecordService.saveDetections(detections);
        return Result.success("已保存 " + savedCount + " 条行为记录");
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping
    public Result<String> addBehavior(@RequestBody BehaviorRecord record) {
        behaviorRecordService.create(record);
        return Result.success("添加行为记录成功");
    }

    @RequireRole({"admin", "teacher"})
    @PutMapping("/{id}")
    public Result<String> updateBehavior(@PathVariable Long id, @RequestBody BehaviorRecord record) {
        behaviorRecordService.update(id, record);
        return Result.success("更新行为记录成功");
    }

    @RequireRole({"admin", "teacher"})
    @DeleteMapping("/{id}")
    public Result<String> deleteBehavior(@PathVariable Long id) {
        behaviorRecordService.delete(id);
        return Result.success("删除行为记录成功");
    }

    @RequireRole({"admin", "teacher"})
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportBehaviors(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            List<BehaviorRecord> behaviors;
            if (startTime != null && endTime != null) {
                behaviors = behaviorRecordService.getByTimeRange(startTime, endTime);
            } else {
                behaviors = behaviorRecordService.getBehaviorList(1, 10000, null, null, null, null).getRecords();
            }

            String[] headers = {"学生姓名", "班级", "行为类型", "行为时间", "置信度", "是否处理", "处理备注"};
            List<Map<String, Object>> data = behaviors.stream()
                    .map(behaviorRecordService::buildExportRow).collect(Collectors.toList());

            byte[] excelData = ExcelExportUtil.exportExcel(headers, data, "行为记录");
            HttpHeaders httpHeaders = new HttpHeaders();
            String fileName = URLEncoder.encode("行为记录_" + LocalDate.now() + ".xlsx", StandardCharsets.UTF_8);
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentDispositionFormData("attachment", fileName);
            return ResponseEntity.ok().headers(httpHeaders).body(excelData);
        } catch (IOException e) {
            throw new com.classroom.attendance.infrastructure.exception.BusinessException(500, "导出失败", e);
        }
    }
}
