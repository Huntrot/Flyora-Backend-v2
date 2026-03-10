package org.example.flyora_backend.service;

import org.example.flyora_backend.DTOs.CreateProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class AIServiceImpl implements AIService {
    
    private final WebClient webClient;
    
    @Autowired
    private FallbackDescriptionService fallbackService;
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public AIServiceImpl(@Qualifier("geminiWebClient") WebClient webClient) {
        this.webClient = webClient;
    }
    
    @Override
    public String generateProductDescription(CreateProductDTO request) {
        try {
            return callGemini(request);
        } catch (Exception e) {
            // Nếu Gemini không khả dụng, sử dụng fallback
            return fallbackService.generateFallbackDescription(request);
        }
    }
    
    private String callGemini(CreateProductDTO request) {
        int maxRetries = 2;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                String prompt = buildPrompt(request);
                
                Map<String, Object> requestBody = new HashMap<>();
                
                List<Map<String, Object>> contents = new ArrayList<>();
                Map<String, Object> content = new HashMap<>();
                
                List<Map<String, Object>> parts = new ArrayList<>();
                Map<String, Object> part = new HashMap<>();
                part.put("text", prompt);
                parts.add(part);
                
                content.put("parts", parts);
                contents.add(content);
                
                requestBody.put("contents", contents);
                
                Map<String, Object> response = webClient.post()
                        .uri("/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .timeout(Duration.ofSeconds(15))
                        .block();
                
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> contentResponse = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> partsResponse = (List<Map<String, Object>>) contentResponse.get("parts");
                Map<String, Object> partResponse = partsResponse.get(0);
                
                return (String) partResponse.get("text");
                
            } catch (WebClientResponseException e) {
                if (e.getStatusCode().value() == 429) {
                    retryCount++;
                    if (retryCount < maxRetries) {
                        try {
                            Thread.sleep(2000 * retryCount); // 2s, 4s
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Bị gián đoạn");
                        }
                        continue;
                    }
                }
                throw e;
            }
        }
        
        throw new RuntimeException("Không thể kết nối Gemini AI");
    }
    
    private String buildPrompt(CreateProductDTO request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tạo mô tả sản phẩm cho cửa hàng chim cảnh:\n\n");
        
        if (request.getName() != null) {
            prompt.append("Tên: ").append(request.getName()).append("\n");
        }
        if (request.getPrice() != null) {
            prompt.append("Giá: ").append(request.getPrice()).append(" VNĐ\n");
        }
        if (request.getMaterial() != null) {
            prompt.append("Chất liệu: ").append(request.getMaterial()).append("\n");
        }
        if (request.getOrigin() != null) {
            prompt.append("Xuất xứ: ").append(request.getOrigin()).append("\n");
        }
        if (request.getUsageTarget() != null) {
            prompt.append("Đối tượng: ").append(request.getUsageTarget()).append("\n");
        }
        if (request.getWeight() != null) {
            prompt.append("Trọng lượng: ").append(request.getWeight()).append(" kg\n");
        }
        if (request.getColor() != null) {
            prompt.append("Màu sắc: ").append(request.getColor()).append("\n");
        }
        if (request.getDimensions() != null) {
            prompt.append("Kích thước: ").append(request.getDimensions()).append("\n");
        }
        
        prompt.append("\nTạo mô tả hấp dẫn, 100-150 từ, tiếng Việt:");
        return prompt.toString();
    }
}