package com.classroom.attendance.modules.face.controller;

import com.classroom.attendance.config.AlgorithmClient;
import com.classroom.attendance.infrastructure.annotation.RequireRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * ③ 安全代理：前端/小程序永不直接接触算法服务地址与密钥（NF1）。
 * 所有算法请求经此后端代理转发，算法 base-url 与 api-key 仅在后端 AlgorithmClient 持有。
 *
 * <p>端点映射（对齐前端 algorithm.js 旧直连路径）：
 * <ul>
 *   <li>POST /api/algorithm/detect    → {algorithm.base-url}/api/behavior/detect</li>
 *   <li>GET  /api/algorithm/health    → {algorithm.base-url}/health</li>
 *   <li>POST /api/algorithm/model-upload → {algorithm.base-url}/api/model/upload</li>
 * </ul>
 *
 * <p>成功时透传算法服务的原始 JSON（与前端原直连行为一致；前端算法实例不经统一 Result 拦截器）。
 * 失败时回退为 {code,message} 兜底，避免裸 500。
 */
@Slf4j
@RestController
@RequestMapping("/api/algorithm")
public class AlgorithmProxyController {

    @Autowired
    private AlgorithmClient algorithmClient;

    @Autowired
    @Qualifier("algorithmRestTemplate")
    private RestTemplate restTemplate;

    @RequireRole({"admin", "teacher"})
    @PostMapping("/detect")
    public ResponseEntity<Map<String, Object>> detect(@RequestBody Map<String, Object> body) {
        return proxy(org.springframework.http.HttpMethod.POST, "/api/behavior/detect", body);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        // 算法 /health 仅注册 GET（methods=['GET']），代理必须按原方法转发，否则 Flask 返回 405。
        return proxy(org.springframework.http.HttpMethod.GET, "/health", null);
    }

    @RequireRole({"admin", "teacher"})
    @PostMapping("/model-upload")
    public ResponseEntity<Map<String, Object>> modelUpload(@RequestBody Map<String, Object> body) {
        return proxy(org.springframework.http.HttpMethod.POST, "/api/model/upload", body);
    }

    private ResponseEntity<Map<String, Object>> proxy(HttpMethod method, String path, Object body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String apiKey = algorithmClient.getApiKey();
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            }
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    algorithmClient.getBaseUrl() + path,
                    method,
                    new org.springframework.http.HttpEntity<>(body, headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> respBody = resp.getBody();
            return ResponseEntity.status(resp.getStatusCode()).body(respBody);
        } catch (HttpStatusCodeException e) {
            log.warn("[AlgorithmProxy] 算法服务返回错误 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("code", e.getStatusCode().value(), "message", "算法服务错误"));
        } catch (ResourceAccessException e) {
            log.warn("[AlgorithmProxy] 算法服务不可达: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("code", 503, "message", "算法服务不可达"));
        } catch (Exception e) {
            log.warn("[AlgorithmProxy] 代理异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("code", 500, "message", "算法代理异常"));
        }
    }
}
