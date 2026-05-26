package com.classroom.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.model.OperationLog;

public interface OperationLogService {
    Page<OperationLog> list(Integer page, Integer size, String username, String operation,
                            String startDate, String endDate);
}
