package com.classroom.attendance.modules.behavior.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.infrastructure.util.SecurityUtil;
import com.classroom.attendance.modules.behavior.dto.BehaviorDetectionDTO;
import com.classroom.attendance.modules.behavior.entity.BehaviorRecord;
import com.classroom.attendance.modules.behavior.mapper.BehaviorRecordMapper;
import com.classroom.attendance.modules.classmgmt.entity.ClassInfo;
import com.classroom.attendance.modules.classmgmt.mapper.ClassMapper;
import com.classroom.attendance.modules.course.entity.Course;
import com.classroom.attendance.modules.course.mapper.CourseMapper;
import com.classroom.attendance.modules.student.entity.Student;
import com.classroom.attendance.modules.student.mapper.StudentMapper;
import com.classroom.attendance.modules.student.service.CreditScoreService;
import com.classroom.attendance.modules.behavior.websocket.BehaviorAlertWebSocket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorRecordService {

    private final BehaviorRecordMapper behaviorRecordMapper;
    private final StudentMapper studentMapper;
    private final ClassMapper classMapper;
    private final CourseMapper courseMapper;
    private final CreditScoreService creditScoreService;

    public Page<BehaviorRecord> getBehaviorList(Integer pageNum, Integer pageSize, Long studentId,
                                                 Collection<Long> classIds, String behaviorType, Integer handled) {
        Page<BehaviorRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BehaviorRecord> w = new LambdaQueryWrapper<>();
        if (studentId != null) w.eq(BehaviorRecord::getStudentId, studentId);
        if (!CollectionUtils.isEmpty(classIds)) w.in(BehaviorRecord::getClassId, classIds);
        if (behaviorType != null && !behaviorType.isEmpty()) w.eq(BehaviorRecord::getBehaviorType, behaviorType);
        if (handled != null) w.eq(BehaviorRecord::getHandled, handled);
        w.orderByDesc(BehaviorRecord::getBehaviorTime);
        Page<BehaviorRecord> result = behaviorRecordMapper.selectPage(page, w);
        fillRelatedData(result.getRecords());
        return result;
    }

    public Page<BehaviorRecord> listForCurrentUser(Integer pageNum, Integer pageSize,
                                                    String studentName, String behaviorType, Integer handled) {
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

        // If studentName filter is provided, resolve it to student ID(s)
        if (studentName != null && !studentName.isEmpty()) {
            List<Long> matchedIds = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>().like(Student::getName, studentName).select(Student::getId))
                    .stream().map(Student::getId).toList();
            if (matchedIds.isEmpty()) {
                Page<BehaviorRecord> empty = new Page<>(pageNum, pageSize);
                empty.setTotal(0);
                return empty;
            }
            filterStudentId = matchedIds.get(0); // Use first match if only filtering by one
            // If multiple students matched, we need to use IN clause instead of EQ
            if (matchedIds.size() > 1) {
                return getByStudentIds(pageNum, pageSize, matchedIds, filterClassIds, behaviorType, handled);
            }
        }

        return getBehaviorList(pageNum, pageSize, filterStudentId, filterClassIds, behaviorType, handled);
    }

    private Page<BehaviorRecord> getByStudentIds(Integer pageNum, Integer pageSize,
                                                  List<Long> studentIds, Collection<Long> classIds,
                                                  String behaviorType, Integer handled) {
        Page<BehaviorRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BehaviorRecord> w = new LambdaQueryWrapper<>();
        w.in(BehaviorRecord::getStudentId, studentIds);
        if (!CollectionUtils.isEmpty(classIds)) w.in(BehaviorRecord::getClassId, classIds);
        if (behaviorType != null && !behaviorType.isEmpty()) w.eq(BehaviorRecord::getBehaviorType, behaviorType);
        if (handled != null) w.eq(BehaviorRecord::getHandled, handled);
        w.orderByDesc(BehaviorRecord::getBehaviorTime);
        Page<BehaviorRecord> result = behaviorRecordMapper.selectPage(page, w);
        fillRelatedData(result.getRecords());
        return result;
    }

    public BehaviorRecord getById(Long id) {
        BehaviorRecord r = behaviorRecordMapper.selectById(id);
        BusinessException.notNull(r, "行为记录不存在");
        return r;
    }

    public List<BehaviorRecord> getByStudentId(Long studentId) {
        return behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getStudentId, studentId)
                        .orderByDesc(BehaviorRecord::getBehaviorTime));
    }

    public List<BehaviorRecord> getByClassId(Long classId) {
        return behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getClassId, classId)
                        .orderByDesc(BehaviorRecord::getBehaviorTime));
    }

    public List<BehaviorRecord> getByType(String behaviorType) {
        return behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getBehaviorType, behaviorType)
                        .orderByDesc(BehaviorRecord::getBehaviorTime));
    }

    public List<BehaviorRecord> getUnhandled() {
        return behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getHandled, 0)
                        .orderByDesc(BehaviorRecord::getBehaviorTime));
    }

    public List<BehaviorRecord> getByTimeRange(LocalDateTime start, LocalDateTime end) {
        return behaviorRecordMapper.selectList(
                new LambdaQueryWrapper<BehaviorRecord>().ge(BehaviorRecord::getBehaviorTime, start)
                        .le(BehaviorRecord::getBehaviorTime, end).orderByDesc(BehaviorRecord::getBehaviorTime));
    }

    public BehaviorRecord create(BehaviorRecord record) {
        behaviorRecordMapper.insert(record);
        applyCreditScore(record);
        return record;
    }

    public BehaviorRecord update(Long id, BehaviorRecord record) {
        BusinessException.notNull(behaviorRecordMapper.selectById(id), "行为记录不存在");
        record.setId(id);
        behaviorRecordMapper.updateById(record);
        return record;
    }

    public void delete(Long id) {
        BusinessException.isTrue(behaviorRecordMapper.deleteById(id) > 0, "行为记录不存在或删除失败");
    }

    public void markAsHandled(Long id, String remark) {
        BehaviorRecord r = behaviorRecordMapper.selectById(id);
        BusinessException.notNull(r, "行为记录不存在");
        r.setHandled(1);
        r.setHandleRemark(remark);
        behaviorRecordMapper.updateById(r);
    }

    public int saveDetections(List<BehaviorDetectionDTO> detections) {
        if (CollectionUtils.isEmpty(detections)) return 0;

        String role = SecurityUtil.getCurrentRole();
        Long userId = SecurityUtil.getCurrentUserId();

        int saved = 0;
        for (BehaviorDetectionDTO dto : detections) {
            BehaviorRecord r = new BehaviorRecord();
            r.setBehaviorType(dto.getBehaviorType());
            r.setConfidence(dto.getConfidence() != null ? BigDecimal.valueOf(dto.getConfidence()) : null);
            r.setBehaviorTime(LocalDateTime.now());
            r.setHandled(0);

            if (dto.getClassId() != null) {
                r.setClassId(dto.getClassId());
            } else if ("teacher".equals(role)) {
                List<Course> courses = courseMapper.selectList(
                        new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, userId).select(Course::getClassId));
                if (!courses.isEmpty()) r.setClassId(courses.get(0).getClassId());
            }

            if (dto.getCourseId() != null) r.setCourseId(dto.getCourseId());
            if (dto.getStudentId() != null) r.setStudentId(dto.getStudentId());
            behaviorRecordMapper.insert(r);
            applyCreditScore(r);
            pushAlertForDetection(r);
            saved++;
        }
        log.info("AI 检测结果已入库，共保存 {} 条", saved);
        return saved;
    }

    private void applyCreditScore(BehaviorRecord r) {
        if (r.getStudentId() == null) return;
        String type = r.getBehaviorType();
        if (List.of("raising_hand", "reading", "writing").contains(type)) {
            creditScoreService.addPositiveBehavior(r.getStudentId());
        } else if ("using_phone".equals(type)) {
            creditScoreService.addPhoneViolation(r.getStudentId());
        } else if (List.of("bowing_head", "leaning_over", "sleeping").contains(type)) {
            creditScoreService.addMinorViolation(r.getStudentId());
        }
    }

    private void pushAlertForDetection(BehaviorRecord r) {
        String type = r.getBehaviorType();
        if (List.of("raising_hand", "reading", "writing").contains(type)) return;

        Map<String, Object> alert = new java.util.LinkedHashMap<>();
        alert.put("behaviorId", r.getId());
        alert.put("behaviorType", type);
        alert.put("classId", r.getClassId());
        alert.put("courseId", r.getCourseId());
        alert.put("confidence", r.getConfidence());
        alert.put("behaviorTime", r.getBehaviorTime().toString());
        if (r.getStudentId() != null) {
            Student s = studentMapper.selectById(r.getStudentId());
            if (s != null) {
                alert.put("studentName", s.getName());
                alert.put("studentNo", s.getStudentNo());
            }
        }
        if (r.getClassId() != null) {
            var ci = classMapper.selectById(r.getClassId());
            if (ci != null) alert.put("className", ci.getClassName());
        }
        BehaviorAlertWebSocket.pushBehaviorAlert(alert);
    }

    public Map<String, Object> buildExportRow(BehaviorRecord b) {
        Student s = b.getStudentId() != null ? studentMapper.selectById(b.getStudentId()) : null;
        ClassInfo ci = b.getClassId() != null ? classMapper.selectById(b.getClassId()) : null;
        return Map.of(
                "studentName", s != null ? s.getName() : "未知",
                "className", ci != null ? ci.getClassName() : "未知",
                "behaviorType", b.getBehaviorType(),
                "behaviorTime", b.getBehaviorTime(),
                "confidence", b.getConfidence(),
                "handled", b.getHandled() == 1 ? "已处理" : "未处理",
                "handleRemark", b.getHandleRemark() != null ? b.getHandleRemark() : "");
    }

    private void fillRelatedData(List<BehaviorRecord> list) {
        if (CollectionUtils.isEmpty(list)) return;
        List<Long> studentIds = list.stream().map(BehaviorRecord::getStudentId).filter(id -> id != null).distinct().collect(Collectors.toList());
        List<Long> classIds = list.stream().map(BehaviorRecord::getClassId).filter(id -> id != null).distinct().collect(Collectors.toList());

        Map<Long, Student> studentMap = studentIds.isEmpty() ? Map.of() :
                studentMapper.selectBatchIds(studentIds).stream().collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, String> classNameMap = classIds.isEmpty() ? Map.of() :
                classMapper.selectBatchIds(classIds).stream().collect(Collectors.toMap(ClassInfo::getId, ClassInfo::getClassName));

        list.forEach(r -> {
            if (r.getStudentId() != null && studentMap.containsKey(r.getStudentId())) {
                r.setStudentName(studentMap.get(r.getStudentId()).getName());
            }
            if (r.getClassId() != null && classNameMap.containsKey(r.getClassId())) {
                r.setClassName(classNameMap.get(r.getClassId()));
            }
        });
    }
}
