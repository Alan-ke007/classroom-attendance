package com.classroom.attendance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.attendance.mapper.BehaviorRecordMapper;
import com.classroom.attendance.model.BehaviorRecord;
import com.classroom.attendance.service.impl.BehaviorRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BehaviorRecordServiceTest {

    @Mock
    private BehaviorRecordMapper behaviorRecordMapper;

    @InjectMocks
    private BehaviorRecordServiceImpl behaviorRecordService;

    private BehaviorRecord sampleRecord;

    @BeforeEach
    void setUp() {
        sampleRecord = new BehaviorRecord();
        sampleRecord.setId(1L);
        sampleRecord.setClassId(1L);
        sampleRecord.setStudentId(100L);
        sampleRecord.setBehaviorType("sleeping");
        sampleRecord.setBehaviorTime(LocalDateTime.now());
        sampleRecord.setConfidence(new BigDecimal("0.95"));
        sampleRecord.setHandled(0);
    }

    @Test
    @DisplayName("添加行为记录成功")
    void testAddBehaviorSuccess() {
        when(behaviorRecordMapper.insert(any(BehaviorRecord.class))).thenReturn(1);

        boolean result = behaviorRecordService.addBehavior(sampleRecord);
        assertTrue(result);
        verify(behaviorRecordMapper).insert(sampleRecord);
    }

    @Test
    @DisplayName("添加行为记录失败")
    void testAddBehaviorFail() {
        when(behaviorRecordMapper.insert(any(BehaviorRecord.class))).thenReturn(0);

        boolean result = behaviorRecordService.addBehavior(sampleRecord);
        assertFalse(result);
    }

    @Test
    @DisplayName("根据ID查询行为记录")
    void testGetBehaviorById() {
        when(behaviorRecordMapper.selectById(1L)).thenReturn(sampleRecord);

        BehaviorRecord found = behaviorRecordService.getBehaviorById(1L);
        assertNotNull(found);
        assertEquals("sleeping", found.getBehaviorType());
        assertEquals(new BigDecimal("0.95"), found.getConfidence());
    }

    @Test
    @DisplayName("查询不存在的行为记录返回null")
    void testGetBehaviorByIdNotFound() {
        when(behaviorRecordMapper.selectById(999L)).thenReturn(null);

        BehaviorRecord found = behaviorRecordService.getBehaviorById(999L);
        assertNull(found);
    }

    @Test
    @DisplayName("标记行为为已处理")
    void testMarkAsHandled() {
        when(behaviorRecordMapper.selectById(1L)).thenReturn(sampleRecord);
        when(behaviorRecordMapper.updateById(any(BehaviorRecord.class))).thenReturn(1);

        boolean result = behaviorRecordService.markAsHandled(1L, "已提醒学生");
        assertTrue(result);
        verify(behaviorRecordMapper).updateById(argThat(record ->
                record.getHandled() == 1 && "已提醒学生".equals(record.getHandleRemark())
        ));
    }

    @Test
    @DisplayName("标记不存在的记录为已处理")
    void testMarkAsHandledNotFound() {
        when(behaviorRecordMapper.selectById(999L)).thenReturn(null);

        boolean result = behaviorRecordService.markAsHandled(999L, "测试");
        assertFalse(result);
        verify(behaviorRecordMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("根据学生ID查询行为记录")
    void testGetBehaviorsByStudentId() {
        when(behaviorRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(sampleRecord));

        List<BehaviorRecord> result = behaviorRecordService.getBehaviorsByStudentId(100L);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("查询未处理的行为记录")
    void testGetUnhandledBehaviors() {
        BehaviorRecord handled = new BehaviorRecord();
        handled.setId(2L);
        handled.setHandled(1);
        sampleRecord.setHandled(0);

        when(behaviorRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(sampleRecord, handled));

        List<BehaviorRecord> result = behaviorRecordService.getUnhandledBehaviors();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("分页查询行为记录")
    void testGetBehaviorList() {
        Page<BehaviorRecord> mockPage = new Page<>(1, 10);
        mockPage.setRecords(List.of(sampleRecord));
        mockPage.setTotal(1);
        when(behaviorRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(mockPage);

        Page<BehaviorRecord> result = behaviorRecordService.getBehaviorList(1, 10, null, null);
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }
}
