package com.classroom.attendance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 算法服务调用类
 * 负责与 Python Flask 算法服务通信
 */
@Slf4j
@Service
public class AlgorithmService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${algorithm.service.url:http://localhost:5000}")
    private String algorithmServiceUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用人脸检测接口
     */
    public List<Map<String, Object>> detectFaces(String imageBase64) {
        log.info("开始调用人脸检测接口");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("image", imageBase64);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            String url = algorithmServiceUrl + "/api/attendance/recognize";
            log.info("请求 URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode != null) {
                    JsonNode facesNode = dataNode.get("faces");
                    if (facesNode != null && facesNode.isArray()) {
                        List<Map<String, Object>> faces = new ArrayList<>();
                        for (JsonNode node : facesNode) {
                            Map<String, Object> face = new HashMap<>();
                            face.put("faceId", node.get("faceId").asText());
                            face.put("confidence", node.get("confidence").asDouble());
                            JsonNode bboxNode = node.get("boundingBox");
                            if (bboxNode != null && bboxNode.isArray()) {
                                Map<String, Object> bbox = new HashMap<>();
                                bbox.put("x", bboxNode.get(0).asInt());
                                bbox.put("y", bboxNode.get(1).asInt());
                                bbox.put("width", bboxNode.get(2).asInt());
                                bbox.put("height", bboxNode.get(3).asInt());
                                face.put("bbox", bbox);
                            }
                            faces.add(face);
                        }
                        log.info("人脸检测完成，检测到 {} 个人脸", faces.size());
                        return faces;
                    }
                }
            }

            log.warn("人脸检测返回空结果");
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("人脸检测调用失败", e);
            throw new RuntimeException("人脸检测服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用人脸检测接口（接受MultipartFile，转为base64）
     */
    public List<Map<String, Object>> detectFaces(MultipartFile imageFile) {
        try {
            String base64 = Base64.getEncoder().encodeToString(imageFile.getBytes());
            return detectFaces(base64);
        } catch (Exception e) {
            log.error("图片转base64失败", e);
            throw new RuntimeException("图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 调用行为识别接口
     */
    public List<Map<String, Object>> detectBehaviors(String imageBase64) {
        log.info("开始调用行为识别接口");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("image", imageBase64);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            String url = algorithmServiceUrl + "/api/behavior/detect";
            log.info("请求 URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode dataNode = rootNode.get("data");
                if (dataNode != null) {
                    JsonNode behaviorsNode = dataNode.get("behaviors");
                    if (behaviorsNode != null && behaviorsNode.isArray()) {
                        List<Map<String, Object>> behaviors = new ArrayList<>();
                        for (JsonNode node : behaviorsNode) {
                            Map<String, Object> behavior = new HashMap<>();
                            behavior.put("behaviorId", node.get("behaviorId").asText());
                            behavior.put("type", node.get("type").asText());
                            behavior.put("description", node.get("description").asText());
                            behavior.put("confidence", node.get("confidence").asDouble());
                            JsonNode bboxNode = node.get("boundingBox");
                            if (bboxNode != null && bboxNode.isArray()) {
                                Map<String, Object> bbox = new HashMap<>();
                                bbox.put("x", bboxNode.get(0).asInt());
                                bbox.put("y", bboxNode.get(1).asInt());
                                bbox.put("width", bboxNode.get(2).asInt());
                                bbox.put("height", bboxNode.get(3).asInt());
                                behavior.put("bbox", bbox);
                            }
                            behaviors.add(behavior);
                        }
                        log.info("行为识别完成，检测到 {} 个行为", behaviors.size());
                        return behaviors;
                    }
                }
            }

            log.warn("行为识别返回空结果");
            return new ArrayList<>();

        } catch (Exception e) {
            log.error("行为识别调用失败", e);
            throw new RuntimeException("行为识别服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用行为识别接口（接受MultipartFile，转为base64）
     */
    public List<Map<String, Object>> detectBehaviors(MultipartFile imageFile) {
        try {
            String base64 = Base64.getEncoder().encodeToString(imageFile.getBytes());
            return detectBehaviors(base64);
        } catch (Exception e) {
            log.error("图片转base64失败", e);
            throw new RuntimeException("图片处理失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            String url = algorithmServiceUrl + "/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.error("算法服务健康检查失败", e);
            return false;
        }
    }
}
