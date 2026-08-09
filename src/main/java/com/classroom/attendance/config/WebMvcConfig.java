package com.classroom.attendance.config;

import com.classroom.attendance.infrastructure.interceptor.LogInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;

    @Value("${file.upload.path:E:/classroom-attendance/uploads}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor).addPathPatterns("/api/**")
                .excludePathPatterns("/api/captcha/generate");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 安全（待 P1 / H5）：/uploads/** 直接静态服务会绕过 FileController 的鉴权与下载安全头，
        // 可能导致越权文件读取或存储型 XSS。应改为统一经下载接口返回，并加
        // Content-Disposition: attachment + X-Content-Type-Options: nosniff。
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
