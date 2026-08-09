package com.classroom.attendance.infrastructure.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 密钥必须从环境变量/密钥管理注入；删除硬编码回退默认值，缺失即启动失败。
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    @PostConstruct
    public void validateSecret() {
        // 密钥必须 >= 32 字节（HS256 要求）。缺失或为空直接阻止启动，杜绝回退默认值。
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret 未配置：必须通过环境变量 JWT_SECRET 注入，禁止硬编码默认值");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret 强度不足：长度须 >= 32 字节（HS256）");
        }
    }

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
