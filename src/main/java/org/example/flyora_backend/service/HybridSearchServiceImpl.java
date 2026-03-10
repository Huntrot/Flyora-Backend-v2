package org.example.flyora_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.flyora_backend.DTOs.ProductListDTO;
import org.example.flyora_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class HybridSearchServiceImpl implements HybridSearchService {

    private final ProductRepository productRepository;
    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public HybridSearchServiceImpl(ProductRepository productRepository, 
                                   @Qualifier("geminiWebClient") WebClient webClient) {
        this.productRepository = productRepository;
        this.webClient = webClient;
    }

    @Override
    public List<ProductListDTO> hybridSearch(String query) {
        if (shouldUseTraditionalSearch(query)) {
            return productRepository.searchByName(query);
        } else {
            return aiSearch(query);
        }
    }

    private boolean shouldUseTraditionalSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String trimmed = query.trim();
        List<ProductListDTO> directResults = productRepository.searchByName(trimmed);
        if (!directResults.isEmpty()) {
            return true;
        }
        
        return false;
    }

    private List<ProductListDTO> aiSearch(String query) {
        try {
            String keywords = extractKeywordsWithAI(query);
            if (keywords != null && !keywords.isEmpty()) {
                List<ProductListDTO> results = productRepository.searchByName(keywords);
                if (!results.isEmpty()) {
                    return results;
                }
            }
        } catch (Exception e) {
            // AI failed, use fallback
        }
        
        String keyword = extractSimpleKeyword(query);
        return productRepository.searchByName(keyword);
    }

    private String extractSimpleKeyword(String query) {
        String lower = query.toLowerCase();
        if (lower.contains("thức ăn") || lower.contains("ăn")) return "thức ăn";
        if (lower.contains("đồ chơi") || lower.contains("chơi")) return "đồ chơi";
        if (lower.contains("lồng")) return "lồng";
        if (lower.contains("phụ kiện")) return "phụ kiện";
        if (lower.contains("hạt")) return "hạt";
        if (lower.contains("vitamin")) return "vitamin";
        // Lấy từ đầu tiên có nghĩa
        String[] words = query.split("\\s+");
        for (String word : words) {
            if (word.length() > 2) return word;
        }
        return query;
    }

    private String extractKeywordsWithAI(String query) {
        int maxRetries = 3;
        int retryDelay = 5000;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    Thread.sleep(retryDelay);
                }
                
                String prompt = "You are a product search assistant for a bird shop.\n" +
                    "User query: \"" + query + "\"\n\n" +
                    "Extract 1-3 MOST IMPORTANT keywords to search products in Vietnamese.\n" +
                    "Focus on: product type (thức ăn, đồ chơi, lồng, phụ kiện), bird type, key features.\n" +
                    "Return ONLY keywords separated by space, NO explanation.\n" +
                    "Examples:\n" +
                    "- 'đồ cho chim non ít rơi vãi' → 'thức ăn'\n" +
                    "- 'tìm đồ chơi cho vẹt' → 'đồ chơi'\n" +
                    "- 'lồng đẹp cho chim cảnh' → 'lồng'";

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
                        .timeout(Duration.ofSeconds(10))
                        .block();

                if (response == null) {
                    continue;
                }

                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    continue;
                }

                Map<String, Object> candidate = candidates.get(0);
                Map<String, Object> contentResponse = (Map<String, Object>) candidate.get("content");
                List<Map<String, Object>> partsResponse = (List<Map<String, Object>>) contentResponse.get("parts");
                Map<String, Object> partResponse = partsResponse.get(0);

                String result = ((String) partResponse.get("text")).trim();
                result = result.replace("\"", "").replace("'", "").replace("\n", " ");
                return result;
                
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests e) {
                if (attempt == maxRetries - 1) {
                    return null;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
}
