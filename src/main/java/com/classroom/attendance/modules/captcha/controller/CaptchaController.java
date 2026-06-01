package com.classroom.attendance.modules.captcha.controller;

import com.classroom.attendance.infrastructure.response.Result;
import com.classroom.attendance.modules.captcha.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/generate")
    public Result<CaptchaService.CaptchaInfo> generate() {
        return Result.success(captchaService.generate());
    }
}
