package com.classroom.attendance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 算法服务客户端配置。
 * 提供独立的 RestTemplate Bean（算法专用），超时从 application.yml 的 algorithm.* 读取，
 * 避免影响仓库内其他（当前无）RestTemplate 使用方。
 * 密钥仅经 @Value 从环境变量 ALGORITHM_API_KEY 注入，绝不硬编码（见 AlgorithmClient）。
 */
@Configuration
public class AlgorithmClientConfig {

    @Value("${algorithm.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${algorithm.read-timeout-ms:8000}")
    private int readTimeoutMs;

    @Bean
    public RestTemplate algorithmRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }
}
