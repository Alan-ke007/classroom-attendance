package com.classroom.attendance.controller;

import com.classroom.attendance.common.Result;
import com.classroom.attendance.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @GetMapping("/generate")
    public Result<CaptchaService.CaptchaInfo> generate() {
        return Result.success(captchaService.generate());
    }
}
