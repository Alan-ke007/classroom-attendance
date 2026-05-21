package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.BehaviorRecordMapper;
import com.classroom.attendance.model.BehaviorRecord;
import com.classroom.attendance.service.BehaviorRecordService;
import com.classroom.attendance.websocket.BehaviorAlertWebSocket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行为识别记录服务实现类
 */
@Slf4j
@Service
public class BehaviorRecordServiceImpl implements BehaviorRecordService {
    
    @Autowired
    private BehaviorRecordMapper behaviorRecordMapper;
    
    @Override
    public Page<BehaviorRecord> getBehaviorList(Integer pageNum, Integer pageSize, Long filterStudentId, Collection<Long> filterClassIds) {
        log.info("接收到分页查询行为记录请求，页码: {}, 每页大小: {}, studentId: {}, classIds: {}", pageNum, pageSize, filterStudentId, filterClassIds);

        Page<BehaviorRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        if (filterStudentId != null) {
            queryWrapper.eq(BehaviorRecord::getStudentId, filterStudentId);
        }
        if (!org.springframework.util.CollectionUtils.isEmpty(filterClassIds)) {
            queryWrapper.in(BehaviorRecord::getClassId, filterClassIds);
        }
        queryWrapper.orderByDesc(BehaviorRecord::getBehaviorTime);
        queryWrapper.orderByDesc(BehaviorRecord::getCreateTime);

        return behaviorRecordMapper.selectPage(page, queryWrapper);
    }
    
    @Override
    public BehaviorRecord getBehaviorById(Long id) {
        log.info("查询行为记录详情，ID: {}", id);
        return behaviorRecordMapper.selectById(id);
    }
    
    @Override
    public boolean addBehavior(BehaviorRecord behaviorRecord) {
        log.info("添加行为记录: 学生ID={}, 行为类型={}", behaviorRecord.getStudentId(), behaviorRecord.getBehaviorType());
        
        int result = behaviorRecordMapper.insert(behaviorRecord);
        
        // 如果添加成功，推送WebSocket预警
        if (result > 0) {
            pushBehaviorAlert(behaviorRecord);
        }
        
        return result > 0;
    }
    
    @Override
    public boolean updateBehavior(BehaviorRecord behaviorRecord) {
        log.info("更新行为记录: {}", behaviorRecord.getId());
        
        // 检查记录是否存在
        BehaviorRecord existingBehavior = behaviorRecordMapper.selectById(behaviorRecord.getId());
        if (existingBehavior == null) {
            log.warn("行为记录不存在，ID: {}", behaviorRecord.getId());
            return false;
        }
        
        int result = behaviorRecordMapper.updateById(behaviorRecord);
        return result > 0;
    }
    
    @Override
    public boolean deleteBehavior(Long id) {
        log.info("删除行为记录，ID: {}", id);
        
        // 检查记录是否存在
        BehaviorRecord existingBehavior = behaviorRecordMapper.selectById(id);
        if (existingBehavior == null) {
            log.warn("行为记录不存在，ID: {}", id);
            return false;
        }
        
        int result = behaviorRecordMapper.deleteById(id);
        return result > 0;
    }
    
    @Override
    public List<BehaviorRecord> getBehaviorsByStudentId(Long studentId) {
        log.info("根据学生ID查询行为记录，学生ID: {}", studentId);
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getStudentId, studentId);
        queryWrapper.orderByDesc(BehaviorRecord::getBehaviorTime);
        return behaviorRecordMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<BehaviorRecord> getBehaviorsByClassId(Long classId) {
        log.info("根据班级ID查询行为记录，班级ID: {}", classId);
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getClassId, classId);
        queryWrapper.orderByDesc(BehaviorRecord::getBehaviorTime);
        return behaviorRecordMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<BehaviorRecord> getBehaviorsByType(String behaviorType) {
        log.info("根据行为类型查询行为记录，类型: {}", behaviorType);
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getBehaviorType, behaviorType);
        queryWrapper.orderByDesc(BehaviorRecord::getBehaviorTime);
        return behaviorRecordMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<BehaviorRecord> getUnhandledBehaviors() {
        log.info("查询未处理的行为记录");
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BehaviorRecord::getHandled, 0);
        queryWrapper.orderByDesc(BehaviorRecord::getBehaviorTime);
        return behaviorRecordMapper.selectList(queryWrapper);
    }
    
    @Override
    public List<BehaviorRecord> getBehaviorsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("根据时间范围查询行为记录");
        LambdaQueryWrapper<BehaviorRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(BehaviorRecord::getBehaviorTime, startTime);
        queryWrapper.le(BehaviorRecord::getBehaviorTime, endTime);
        queryWrapper.orderByDesc(BehaviorRecord::getBehaviorTime);
        return behaviorRecordMapper.selectList(queryWrapper);
    }
    
    @Override
    public boolean markAsHandled(Long id, String handleRemark) {
        log.info("标记行为记录为已处理，ID: {}", id);
        
        BehaviorRecord behaviorRecord = behaviorRecordMapper.selectById(id);
        if (behaviorRecord == null) {
            log.warn("行为记录不存在，ID: {}", id);
            return false;
        }
        
        behaviorRecord.setHandled(1);
        behaviorRecord.setHandleRemark(handleRemark);
        
        int result = behaviorRecordMapper.updateById(behaviorRecord);
        return result > 0;
    }
    
    /**
     * 推送行为预警到WebSocket
     */
    private void pushBehaviorAlert(BehaviorRecord behaviorRecord) {
        try {
            Map<String, Object> alertData = new HashMap<>();
            alertData.put("id", behaviorRecord.getId());
            alertData.put("studentId", behaviorRecord.getStudentId());
            alertData.put("classId", behaviorRecord.getClassId());
            alertData.put("behaviorType", behaviorRecord.getBehaviorType());
            alertData.put("behaviorTime", behaviorRecord.getBehaviorTime());
            alertData.put("confidence", behaviorRecord.getConfidence());
            alertData.put("message", getBehaviorMessage(behaviorRecord.getBehaviorType()));
            alertData.put("level", getAlertLevel(behaviorRecord.getBehaviorType()));
            
            BehaviorAlertWebSocket.pushBehaviorAlert(alertData);
            log.info("行为预警已推送: 类型={}, 时间={}", 
                behaviorRecord.getBehaviorType(), behaviorRecord.getBehaviorTime());
        } catch (Exception e) {
            log.error("推送行为预警失败", e);
        }
    }
    
    /**
     * 获取行为类型的中文描述
     */
    private String getBehaviorMessage(String behaviorType) {
        if (behaviorType == null) return "检测到未知行为";
        return switch (behaviorType.toLowerCase()) {
            case "sleeping" -> "检测到学生睡觉";
            case "phone" -> "检测到学生玩手机";
            case "eating" -> "检测到学生吃东西";
            case "talking" -> "检测到学生交头接耳";
            case "absent" -> "检测到学生缺勤";
            default -> "检测到异常行为: " + behaviorType;
        };
    }

    /**
     * 获取预警级别
     */
    private String getAlertLevel(String behaviorType) {
        if (behaviorType == null) return "low";
        return switch (behaviorType.toLowerCase()) {
            case "sleeping", "phone" -> "high";
            case "eating", "talking" -> "medium";
            default -> "low";
        };
    }
}
