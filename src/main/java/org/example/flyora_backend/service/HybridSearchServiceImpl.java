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
        log.info("=== Hybrid Search Start: '{}' ===", query);
        if (shouldUseTraditionalSearch(query)) {
            log.info("Using TRADITIONAL search");
            return productRepository.searchByName(query);
        } else {
            log.info("Using AI search");
            return aiSearch(query);
        }
    }

    private boolean shouldUseTraditionalSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }

        String trimmed = query.trim().toLowerCase();
        
        // Nếu query quá ngắn (1-2 từ đơn giản), dùng traditional
        String[] words = trimmed.split("\\s+");
        if (words.length <= 2 && !containsComplexPattern(trimmed)) {
            List<ProductListDTO> directResults = productRepository.searchByName(trimmed);
            if (!directResults.isEmpty()) {
                return true;
            }
        }
        
        // Nếu query phức tạp (có "cho", "tìm", "muốn", v.v.), dùng AI
        return false;
    }
    
    private boolean containsComplexPattern(String query) {
        // Kiểm tra xem có pattern phức tạp không
        return query.contains(" cho ") || 
               query.contains("tìm ") || 
               query.contains("muốn ") || 
               query.contains("cần ") ||
               query.contains(" của ") ||
               query.contains(" dành ");
    }

    private List<ProductListDTO> aiSearch(String query) {
        try {
            Map<String, String> extractedKeywords = extractKeywordsWithAI(query);
            if (extractedKeywords != null) {
                String productKeyword = extractedKeywords.get("product");
                String birdKeyword = extractedKeywords.get("bird");
                
                List<ProductListDTO> results = productRepository.searchByNameAndBirdType(productKeyword, birdKeyword);
                if (!results.isEmpty()) {
                    return results;
                }
                
                // Nếu AI đã trích xuất được, dùng luôn kết quả đó cho fallback
                if (productKeyword != null || birdKeyword != null) {
                    return results; // Trả về empty list thay vì tìm lại
                }
            }
        } catch (Exception e) {
            // AI search failed, fallback to simple search
        }
        
        Map<String, String> fallbackKeywords = extractSimpleKeyword(query);
        List<ProductListDTO> results = productRepository.searchByNameAndBirdType(
            fallbackKeywords.get("product"), 
            fallbackKeywords.get("bird")
        );
        return results;
    }

    private Map<String, String> extractSimpleKeyword(String query) {
        Map<String, String> keywords = new HashMap<>();
        String lower = query.toLowerCase();
        
        // Trích xuất loại sản phẩm
        String productKeyword = null;
        if (lower.contains("thức ăn") || lower.contains("ăn")) productKeyword = "thức ăn";
        else if (lower.contains("đồ chơi") || lower.contains("chơi")) productKeyword = "đồ chơi";
        else if (lower.contains("lồng")) productKeyword = "lồng";
        else if (lower.contains("phụ kiện")) productKeyword = "phụ kiện";
        else if (lower.contains("hạt")) productKeyword = "hạt";
        else if (lower.contains("vitamin")) productKeyword = "vitamin";
        
        // Trích xuất loại chim
        String birdKeyword = null;
        if (lower.contains("vẹt")) birdKeyword = "vẹt";
        else if (lower.contains("chào mào")) birdKeyword = "chào mào";
        else if (lower.contains("chim cảnh")) birdKeyword = "chim cảnh";
        else if (lower.contains("yến")) birdKeyword = "yến";
        else if (lower.contains("chích chòe")) birdKeyword = "chích chòe";
        else if (lower.contains("hoạ mi")) birdKeyword = "hoạ mi";
        else if (lower.contains("cu gáy")) birdKeyword = "cu gáy";
        else if (lower.contains("bồ câu")) birdKeyword = "bồ câu";
        
        // Nếu không tìm thấy gì, lấy từ đầu tiên có nghĩa
        if (productKeyword == null && birdKeyword == null) {
            String[] words = query.split("\\s+");
            for (String word : words) {
                if (word.length() > 2) {
                    productKeyword = word;
                    break;
                }
            }
        }
        
        keywords.put("product", productKeyword);
        keywords.put("bird", birdKeyword);
        return keywords;
    }

    private Map<String, String> extractKeywordsWithAI(String query) {
        int maxRetries = 3;
        int retryDelay = 5000;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                if (attempt > 0) {
                    Thread.sleep(retryDelay);
                }
                
                String prompt = "You are a product search assistant for a bird shop.\n" +
                    "User query: \"" + query + "\"\n\n" +
                    "Extract keywords in Vietnamese and return in JSON format with 2 fields:\n" +
                    "1. \"product\": product type (thức ăn, đồ chơi, lồng, phụ kiện, hạt, vitamin) or null\n" +
                    "2. \"bird\": bird type (vẹt, chào mào, chim cảnh, yến, chích chòe, hoạ mi, cu gáy, bồ câu) or null\n\n" +
                    "Return ONLY valid JSON, NO explanation, NO markdown.\n\n" +
                    "Examples:\n" +
                    "- 'đồ ăn cho chào mào' → {\"product\":\"thức ăn\",\"bird\":\"chào mào\"}\n" +
                    "- 'tìm đồ chơi cho vẹt' → {\"product\":\"đồ chơi\",\"bird\":\"vẹt\"}\n" +
                    "- 'lồng đẹp cho chim cảnh' → {\"product\":\"lồng\",\"bird\":\"chim cảnh\"}\n" +
                    "- 'thức ăn cho chim' → {\"product\":\"thức ăn\",\"bird\":null}\n" +
                    "- 'đồ chơi' → {\"product\":\"đồ chơi\",\"bird\":null}";

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
                // Loại bỏ markdown code block nếu có
                result = result.replace("```json", "").replace("```", "").trim();
                
                // Parse JSON response
                try {
                    Map<String, String> keywords = new HashMap<>();
                    // Simple JSON parsing
                    result = result.replace("{", "").replace("}", "");
                    String[] pairs = result.split(",");
                    
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":");
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replace("\"", "");
                            String value = keyValue[1].trim().replace("\"", "");
                            if (!value.equals("null") && !value.isEmpty()) {
                                keywords.put(key, value);
                            } else {
                                keywords.put(key, null);
                            }
                        }
                    }
                    return keywords;
                } catch (Exception parseEx) {
                    return null;
                }
                
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
