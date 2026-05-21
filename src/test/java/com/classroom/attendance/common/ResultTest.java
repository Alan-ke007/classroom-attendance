package com.classroom.attendance.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void testSuccessNoArgs() {
        Result<String> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testSuccessWithData() {
        Result<String> result = Result.success("test");
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("test", result.getData());
    }

    @Test
    void testSuccessWithMessageAndData() {
        Result<String> result = Result.success("自定义成功", "data");
        assertEquals(200, result.getCode());
        assertEquals("自定义成功", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    void testError() {
        Result<String> result = Result.error("出错");
        assertEquals(500, result.getCode());
        assertEquals("出错", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void testFail() {
        Result<String> result = Result.fail("参数错误");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
    }

    @Test
    void testFailWithCode() {
        Result<String> result = Result.fail(404, "未找到");
        assertEquals(404, result.getCode());
        assertEquals("未找到", result.getMessage());
    }

    @Test
    void testUnauthorized() {
        Result<String> result = Result.unauthorized("未登录");
        assertEquals(401, result.getCode());
        assertEquals("未登录", result.getMessage());
    }

    @Test
    void testSuccessWithComplexData() {
        Map<String, Object> map = Map.of("total", 100, "records", List.of("a", "b"));
        Result<Map<String, Object>> result = Result.success(map);
        assertEquals(200, result.getCode());
        assertEquals(100, result.getData().get("total"));
    }

    @Test
    void testPageResult() {
        PageResult<String> page = new PageResult<>(
                List.of("item1", "item2"), 50L, 2L, 10L);

        assertEquals(50L, page.getTotal());
        assertEquals(2L, page.getCurrent());
        assertEquals(10L, page.getSize());
        assertEquals(5L, page.getPages());
        assertTrue(page.getHasPrevious());
        assertTrue(page.getHasNext());
        assertEquals(2, page.getRecords().size());
    }
}
