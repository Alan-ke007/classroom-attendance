package com.classroom.attendance.config;

import com.classroom.attendance.infrastructure.exception.AlgoUnavailableException;
import com.classroom.attendance.infrastructure.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;

/**
 * 算法服务客户端：封装对算法服务 {@code POST /api/face/extract} 的调用。
 *
 * <p>信任边界（R6）：仅在后端→算法受信链路调用，密钥仅由本组件经环境变量注入，
 * 小程序/前端永不可见。本客户端只做「提特征」，不做 1:1 判定（判定在 FaceService）。
 *
 * <p>错误映射（与 PRD §3.6 一致）：
 * <ul>
 *   <li>算法 401/503/连接超时/网络错 → {@link AlgoUnavailableException}(40005)，不得裸 500。</li>
 *   <li>无脸(40010)/多脸(40011)/解码失败(400) → BusinessException(40002/40003/40004)。</li>
 * </ul>
 *
 * <p>F10 mock：{@code ALGORITHM_MOCK=true} 时跳过真实调用，返回确定性固定 embedding（全 0.1，512 维）。
 */
@Slf4j
@Component
public class AlgorithmClient {

    public static final int EMBEDDING_DIM = 512;

    @Value("${algorithm.base-url:http://localhost:5000}")
    private String baseUrl;

    @Value("${algorithm.api-key:}")
    private String apiKey;

    @Value("${algorithm.mock:false}")
    private boolean mock;

    @Value("${algorithm.threshold:0.4}")
    private double threshold;

    @Value("${algorithm.model:buffalo_l}")
    private String model;

    private final RestTemplate restTemplate;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String EXTRACT_PATH = "/api/face/extract";

    @Autowired
    public AlgorithmClient(@Qualifier("algorithmRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean isMock() {
        return mock;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getModel() {
        return model;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    @PostConstruct
    public void warnIfKeyMissing() {
        if (!mock && (apiKey == null || apiKey.isBlank())) {
            log.warn("[AlgorithmClient] 未配置 ALGORITHM_API_KEY 且非 mock 模式：真实人脸核验将因算法 401 而返回"
                    + " ALGO_UNAVAILABLE(40005)，不会静默放行真实核验。");
        }
    }

    /**
     * 调用算法服务提取人脸特征。
     *
     * @param base64Image 图片 base64（允许 data:image/...;base64, 前缀）
     * @return 特征向量（512 维 float）与检测到的人脸数
     */
    public ExtractResult extract(String base64Image) {
        if (mock) {
            float[] emb = new float[EMBEDDING_DIM];
            Arrays.fill(emb, 0.1f);
            return new ExtractResult(emb, 1);
        }

        long start = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (apiKey != null && !apiKey.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            }
            Map<String, String> body = Map.of("image", base64Image == null ? "" : base64Image);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<AlgorithmResponse> resp = restTemplate.postForEntity(
                    baseUrl + EXTRACT_PATH, request, AlgorithmResponse.class);
            long cost = System.currentTimeMillis() - start;

            AlgorithmResponse ar = resp.getBody();
            if (ar == null || ar.getData() == null || ar.getData().getEmbedding() == null) {
                log.warn("[AlgorithmClient] extract 响应异常 cost={}ms body={}", cost, resp.getBody());
                throw new AlgoUnavailableException("算法服务返回空响应");
            }
            double[] emb = ar.getData().getEmbedding();
            int faceCount = ar.getData().getFaceCount();
            if (emb.length != EMBEDDING_DIM) {
                log.warn("[AlgorithmClient] extract 特征维度异常 cost={}ms dim={}", cost, emb.length);
                throw new AlgoUnavailableException("算法返回特征维度异常: " + emb.length);
            }
            if (faceCount == 0) throw new BusinessException(40002, "未检测到人脸");
            if (faceCount > 1) throw new BusinessException(40003, "检测到多张人脸");

            log.info("[AlgorithmClient] extract 成功 cost={}ms faceCount={}", cost, faceCount);
            float[] f = new float[EMBEDDING_DIM];
            for (int i = 0; i < EMBEDDING_DIM; i++) f[i] = (float) emb[i];
            return new ExtractResult(f, faceCount);
        } catch (AlgoUnavailableException e) {
            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (HttpStatusCodeException e) {
            long cost = System.currentTimeMillis() - start;
            int status = e.getStatusCode().value();
            if (status == 401 || status == 503 || status >= 500) {
                log.warn("[AlgorithmClient] extract 算法不可达 status={} cost={}ms", status, cost);
                throw new AlgoUnavailableException("算法服务不可达");
            }
            ErrorBody eb = parseError(e.getResponseBodyAsString());
            int code = eb != null ? eb.code : 400;
            String msg = eb != null && eb.message != null ? eb.message : "图片处理失败";
            log.warn("[AlgorithmClient] extract 业务错误 status={} code={} msg={} cost={}ms", status, code, msg, cost);
            throw mapExtractError(code, msg);
        } catch (ResourceAccessException e) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[AlgorithmClient] extract 网络/超时 cost={}ms err={}", cost, e.getMessage());
            throw new AlgoUnavailableException("算法服务连接超时或不可达");
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.warn("[AlgorithmClient] extract 未知错误 cost={}ms err={}", cost, e.getMessage());
            throw new AlgoUnavailableException("算法服务调用失败");
        }
    }

    private BusinessException mapExtractError(int code, String msg) {
        return switch (code) {
            case 40010 -> new BusinessException(40002, "未检测到人脸");
            case 40011 -> new BusinessException(40003, "检测到多张人脸");
            case 400 -> new BusinessException(40004, msg);
            case 413 -> new BusinessException(400, "图片数据过大，超出大小限制");
            default -> new BusinessException(code, msg);
        };
    }

    private ErrorBody parseError(String body) {
        if (body == null || body.isBlank()) return null;
        try {
            return OBJECT_MAPPER.readValue(body, ErrorBody.class);
        } catch (Exception e) {
            return null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlgorithmResponse {
        public int code;
        public String message;
        public ExtractData data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractData {
        public double[] embedding;
        public int faceCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorBody {
        public int code;
        public String message;
    }

    /** 提取结果：512 维特征 + 人脸数。 */
    public static class ExtractResult {
        private final float[] embedding;
        private final int faceCount;

        public ExtractResult(float[] embedding, int faceCount) {
            this.embedding = embedding;
            this.faceCount = faceCount;
        }

        public float[] getEmbedding() {
            return embedding;
        }

        public int getFaceCount() {
            return faceCount;
        }
    }
}
