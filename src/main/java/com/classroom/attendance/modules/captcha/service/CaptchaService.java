package com.classroom.attendance.modules.captcha.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaService {

    private final Map<String, CaptchaData> captchaStore = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final long EXPIRATION_MS = 5 * 60 * 1000;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    public record CaptchaInfo(String captchaId, String imageBase64) {}
    private record CaptchaData(String code, long timestamp) {}

    public CaptchaInfo generate() {
        String captchaId = System.currentTimeMillis() + "_" + random.nextInt(100000);
        String code = generateCode();
        captchaStore.put(captchaId, new CaptchaData(code, System.currentTimeMillis()));
        String imageBase64 = generateImage(code);
        return new CaptchaInfo(captchaId, imageBase64);
    }

    public boolean verify(String captchaId, String userInput) {
        CaptchaData data = captchaStore.get(captchaId);
        if (data == null) return false;
        if (System.currentTimeMillis() - data.timestamp() > EXPIRATION_MS) {
            captchaStore.remove(captchaId);
            return false;
        }
        captchaStore.remove(captchaId);
        return data.code().equalsIgnoreCase(userInput.trim());
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) sb.append(CHARS[random.nextInt(CHARS.length)]);
        return sb.toString();
    }

    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(new Color(245, 245, 245));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("Arial", Font.BOLD, 24));

        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT), random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(150) + 50));
            int x = 15 + i * 25 + random.nextInt(5);
            int y = 28 + random.nextInt(5);
            double angle = (random.nextDouble() - 0.5) * 0.4;
            g.rotate(angle, x, y);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
            g.rotate(-angle, x, y);
        }
        g.dispose();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void cleanExpired() {
        long now = System.currentTimeMillis();
        captchaStore.entrySet().removeIf(e -> now - e.getValue().timestamp() > EXPIRATION_MS);
    }
}
