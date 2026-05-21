package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.BehaviorRecord;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 行为识别记录服务接口
 */
public interface BehaviorRecordService {
    
    /**
     * 分页查询行为记录列表
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<BehaviorRecord> getBehaviorList(Integer pageNum, Integer pageSize, Long filterStudentId, Collection<Long> filterClassIds);
    
    /**
     * 根据ID查询行为记录
     * @param id 行为记录ID
     * @return 行为记录信息
     */
    BehaviorRecord getBehaviorById(Long id);
    
    /**
     * 添加行为记录
     * @param behaviorRecord 行为记录信息
     * @return 是否成功
     */
    boolean addBehavior(BehaviorRecord behaviorRecord);
    
    /**
     * 更新行为记录
     * @param behaviorRecord 行为记录信息
     * @return 是否成功
     */
    boolean updateBehavior(BehaviorRecord behaviorRecord);
    
    /**
     * 删除行为记录
     * @param id 行为记录ID
     * @return 是否成功
     */
    boolean deleteBehavior(Long id);
    
    /**
     * 根据学生ID查询行为记录
     * @param studentId 学生ID
     * @return 行为记录列表
     */
    List<BehaviorRecord> getBehaviorsByStudentId(Long studentId);
    
    /**
     * 根据班级ID查询行为记录
     * @param classId 班级ID
     * @return 行为记录列表
     */
    List<BehaviorRecord> getBehaviorsByClassId(Long classId);
    
    /**
     * 根据行为类型查询行为记录
     * @param behaviorType 行为类型
     * @return 行为记录列表
     */
    List<BehaviorRecord> getBehaviorsByType(String behaviorType);
    
    /**
     * 查询未处理的行为记录
     * @return 行为记录列表
     */
    List<BehaviorRecord> getUnhandledBehaviors();
    
    /**
     * 根据时间范围查询行为记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 行为记录列表
     */
    List<BehaviorRecord> getBehaviorsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 标记行为记录为已处理
     * @param id 行为记录ID
     * @param handleRemark 处理备注
     * @return 是否成功
     */
    boolean markAsHandled(Long id, String handleRemark);
}
