package com.classroom.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.OperationLogMapper;
import com.classroom.attendance.model.OperationLog;
import com.classroom.attendance.service.OperationLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Override
    public Page<OperationLog> list(Integer page, Integer size, String username,
                                   String operation, String startDate, String endDate) {
        Page<OperationLog> pageObj = new Page<>(page, size);

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(username), OperationLog::getUsername, username);
        wrapper.eq(StringUtils.isNotBlank(operation), OperationLog::getOperation, operation);

        if (StringUtils.isNotBlank(startDate)) {
            wrapper.ge(OperationLog::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.isNotBlank(endDate)) {
            wrapper.le(OperationLog::getCreateTime, endDate + " 23:59:59");
        }

        wrapper.orderByDesc(OperationLog::getId);

        return operationLogMapper.selectPage(pageObj, wrapper);
    }
}
