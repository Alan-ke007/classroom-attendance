package com.classroom.attendance.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKeyForClassroomAttendanceSystem2024}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String role, String realName) {
        return generateToken(userId, username, role, realName, null, null);
    }

    public String generateToken(Long userId, String username, String role, String realName,
                                Long studentId, Long classId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        claims.put("realName", realName);
        if (studentId != null) claims.put("studentId", studentId);
        if (classId != null) claims.put("classId", classId);

        Date now = new Date();
        return Jwts.builder().claims(claims).subject(username)
                .issuedAt(now).expiration(new Date(now.getTime() + expiration))
                .signWith(getSecretKey()).compact();
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaimsFromToken(token).get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        return getClaimsFromToken(token).get("role", String.class);
    }

    public String generateToken(Map<String, Object> claims, long expirationMs) {
        Date now = new Date();
        return Jwts.builder().claims(claims).issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(getSecretKey()).compact();
    }

    public Map<String, Object> parseToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Map<String, Object> result = new HashMap<>();
        claims.forEach(result::put);
        return result;
    }

    public boolean validateToken(String token) {
        try {
            return !getClaimsFromToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
