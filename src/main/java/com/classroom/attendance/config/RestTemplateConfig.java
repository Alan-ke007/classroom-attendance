package com.classroom.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置类
 * 用于调用 Python 算法服务
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 配置 RestTemplate Bean
     * 设置超时时间和连接参数
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 连接超时时间（毫秒）
        factory.setConnectTimeout(5000);
        
        // 读取超时时间（毫秒）- 算法推理可能需要较长时间
        factory.setReadTimeout(30000);
        
        return new RestTemplate(factory);
    }
}
