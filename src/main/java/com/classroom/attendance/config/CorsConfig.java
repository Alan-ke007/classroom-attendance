package com.classroom.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置（安全收紧 H4：来源白名单化，禁止通配符 "*"）
 */
@Configuration
public class CorsConfig {

    // 生产环境来源白名单，逗号分隔；通过环境变量 CORS_ALLOWED_ORIGINS 注入。
    // 开发环境本机 localhost/127.0.0.1 任意端口已在代码中内置放行（见下方 corsFilter）。
    @Value("${cors.allowed-origins:}")
    private String allowedOriginsRaw;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许来源：
        //  1) 开发环境：本机 localhost / 127.0.0.1 任意端口（仅本机，安全可接受，兼容 Vite 5173 等）
        //  2) 生产环境：通过 CORS_ALLOWED_ORIGINS 追加具体前端域名，禁止通配符 "*"
        List<String> patterns = new ArrayList<>(List.of(
                "http://localhost:*", "https://localhost:*",
                "http://127.0.0.1:*", "https://127.0.0.1:*"));
        if (allowedOriginsRaw != null && !allowedOriginsRaw.isBlank()) {
            Arrays.stream(allowedOriginsRaw.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty())
                    .forEach(patterns::add);
        }
        patterns.forEach(config::addAllowedOriginPattern);
        // ② 安全：鉴权改走 httpOnly Cookie，需携带凭证；配合显式来源（无通配符 "*"）使用。
        config.setAllowCredentials(true);
        // 显式允许的请求头（含 Cookie，供凭证跨域携带）；不使用 "*" 以契合凭证模式。
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Cookie", "X-Requested-With"));
        // 收敛请求方法，避免暴露不必要动词
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
