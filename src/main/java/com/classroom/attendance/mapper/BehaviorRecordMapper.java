package com.classroom.attendance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.attendance.model.BehaviorRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 行为识别记录数据访问接口
 */
@Mapper
public interface BehaviorRecordMapper extends BaseMapper<BehaviorRecord> {
}
