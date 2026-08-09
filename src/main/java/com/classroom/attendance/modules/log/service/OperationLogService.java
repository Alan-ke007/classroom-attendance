package com.classroom.attendance.modules.log.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.modules.log.entity.OperationLog;
import com.classroom.attendance.modules.log.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public Page<OperationLog> list(Integer page, Integer size, String username, String operation, String startDate, String endDate) {
        Page<OperationLog> p = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> w = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) w.like(OperationLog::getUsername, username);
        if (operation != null && !operation.isEmpty()) w.eq(OperationLog::getOperation, operation);
        if (startDate != null && !startDate.isEmpty()) w.ge(OperationLog::getCreateTime, startDate + " 00:00:00");
        if (endDate != null && !endDate.isEmpty()) w.le(OperationLog::getCreateTime, endDate + " 23:59:59");
        w.orderByDesc(OperationLog::getCreateTime);
        return operationLogMapper.selectPage(p, w);
    }

    public void delete(Long id) {
        operationLogMapper.deleteById(id);
    }

    public void clearAll() {
        operationLogMapper.delete(new LambdaQueryWrapper<>());
    }
}
