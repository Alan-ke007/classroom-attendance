package com.classroom.attendance.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForClassroomAttendanceSystem2024!!");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }

    @Test
    void testGenerateToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testParseToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        assertEquals(1L, userId);
        assertEquals("admin", username);
        assertEquals("admin", role);
    }

    @Test
    void testValidateToken() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1ms
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {
        }
        assertFalse(jwtUtil.validateToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
        assertFalse(jwtUtil.validateToken(""));
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void testTokenWithDifferentRoles() {
        String studentToken = jwtUtil.generateToken(2L, "202101001", "student");
        String teacherToken = jwtUtil.generateToken(3L, "teacher1", "teacher");

        assertEquals("student", jwtUtil.getRoleFromToken(studentToken));
        assertEquals("teacher", jwtUtil.getRoleFromToken(teacherToken));
    }

    @Test
    void testParseTokenReturnsAllClaims() {
        String token = jwtUtil.generateToken(1L, "admin", "admin");
        Map<String, Object> claims = jwtUtil.parseToken(token);

        assertEquals(1L, ((Number) claims.get("userId")).longValue());
        assertEquals("admin", claims.get("username"));
        assertEquals("admin", claims.get("role"));
    }
}
