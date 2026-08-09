package com.classroom.attendance.infrastructure.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String SECRET = "this-is-a-test-secret-key-at-least-32-bytes!!";

    @BeforeEach
    void setUp() {
        // @Value 在纯单元测试中不被注入，这里用反射设置（>=32 字节以满足 HS256 要求）
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateAndValidate_roundTrip() {
        String token = jwtUtil.generateToken(1L, "alice", "admin", "Alice", 10L, 20L);
        assertTrue(jwtUtil.validateToken(token), "合法令牌应校验通过");
        assertEquals("alice", jwtUtil.getUsernameFromToken(token));
        assertEquals(1L, jwtUtil.getUserIdFromToken(token));
        assertEquals("admin", jwtUtil.getRoleFromToken(token));
    }

    @Test
    void tamperedToken_isInvalid() {
        String token = jwtUtil.generateToken(1L, "alice", "admin", "Alice");
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("ab") ? "cd" : "ab");
        assertFalse(jwtUtil.validateToken(tampered), "被篡改的令牌应校验失败");
    }

    @Test
    void expiredToken_isInvalid() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("username", "alice");
        claims.put("role", "admin");
        claims.put("realName", "Alice");
        // 过期时间设为过去（-1000ms）
        String expired = jwtUtil.generateToken(claims, -1000L);
        assertFalse(jwtUtil.validateToken(expired), "已过期的令牌应校验失败");
    }
}
