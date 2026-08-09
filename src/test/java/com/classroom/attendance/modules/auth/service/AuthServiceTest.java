package com.classroom.attendance.modules.auth.service;

import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.classroom.attendance.modules.auth.entity.PasswordResetToken;
import com.classroom.attendance.modules.auth.entity.User;
import com.classroom.attendance.modules.auth.mapper.PasswordResetTokenMapper;
import com.classroom.attendance.modules.auth.mapper.StudentMapper;
import com.classroom.attendance.modules.auth.mapper.UserMapper;
import com.classroom.attendance.modules.captcha.service.CaptchaService;
import com.classroom.attendance.infrastructure.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CaptchaService captchaService;
    @Mock
    private PasswordResetTokenMapper resetTokenMapper;

    @InjectMocks
    private AuthService service;

    // ② 测试夹具：固定原始令牌，便于断言"仅存哈希、一次性"语义。
    private static final String RAW_TOKEN = "plainrawtoken-0123456789abcdef";

    @BeforeEach
    void setUp() {
        // spy 包装以便桩掉 package-private 的令牌生成方法（生产用 SecureRandom，测试需可预测）
        service = spy(service);
        doReturn(RAW_TOKEN).when(service).generateRawToken();
    }

    // C4: 令牌仅存 SHA-256 哈希，绝不以明文落库
    @Test
    void requestPasswordReset_storesOnlyHash() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@x.com");
        when(userMapper.findByUsername("alice")).thenReturn(user);
        when(resetTokenMapper.insert(any())).thenReturn(1);

        service.requestPasswordReset("alice", "alice@x.com");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokenMapper).insert(captor.capture());
        PasswordResetToken saved = captor.getValue();

        String expectedHash = sha256Hex(RAW_TOKEN);
        assertEquals(expectedHash, saved.getTokenHash(), "落库应为原始令牌的 SHA-256 哈希");
        assertNotEquals(RAW_TOKEN, saved.getTokenHash(), "原始明文令牌不得落库");
        assertTrue(saved.getTokenHash().matches("[0-9a-f]{64}"), "哈希应为 64 位十六进制");
    }

    // C4: 令牌一次性——消费后立即作废，二次使用应失败
    @Test
    void resetPasswordWithToken_isOneTime() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        when(userMapper.findByUsername("alice")).thenReturn(user);

        PasswordResetToken record = PasswordResetToken.builder()
                .username("alice")
                .tokenHash(sha256Hex(RAW_TOKEN))
                .expiry(LocalDateTime.now().plusMinutes(15))
                .used(0)
                .build();
        // selectOne 仅在 used==0 时返回该记录（模拟"一次性"语义）
        when(resetTokenMapper.selectOne(any())).thenAnswer(inv ->
                (record.getUsed() != null && record.getUsed() == 0) ? record : null);
        when(passwordEncoder.encode(anyString())).thenReturn("ENCODED");
        when(userMapper.updateById(any())).thenReturn(1);
        when(resetTokenMapper.updateById(any())).thenReturn(1);

        // 首次使用：成功改密
        service.resetPasswordWithToken(RAW_TOKEN, "newpass123");
        verify(userMapper).updateById(any());

        // 二次使用：令牌已作废 → 应抛异常
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.resetPasswordWithToken(RAW_TOKEN, "another123"));
        assertTrue(ex.getMessage().contains("无效或已过期"));
    }

    // C4: 防用户枚举——账户不存在时不抛异常、不落库令牌，返回统一提示
    @Test
    void requestPasswordReset_antiEnumeration() {
        when(userMapper.findByUsername("ghost")).thenReturn(null);

        assertDoesNotThrow(() -> service.requestPasswordReset("ghost", "ghost@x.com"));
        verify(resetTokenMapper, never()).insert(any());
    }

    // C4: 限流——同一来源 60s 内最多 5 次，第 6 次应被拒绝
    @Test
    void requestPasswordReset_rateLimited() {
        when(userMapper.findByUsername(anyString())).thenReturn(null); // 始终"账户不存在"，仅触发限流计数

        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> service.requestPasswordReset("u" + i, "e@x.com"));
        }
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.requestPasswordReset("u6", "e@x.com"));
        assertTrue(ex.getMessage().contains("频繁"));
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
