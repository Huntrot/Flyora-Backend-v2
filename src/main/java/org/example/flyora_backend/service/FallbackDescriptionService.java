package org.example.flyora_backend.service;

import org.example.flyora_backend.DTOs.CreateProductDTO;
import org.springframework.stereotype.Service;

@Service
public class FallbackDescriptionService {
    
    public String generateFallbackDescription(CreateProductDTO request) {
        StringBuilder description = new StringBuilder();
        
        if (request.getName() != null) {
            description.append(request.getName()).append(" - ");
        }
        
        description.append("Sản phẩm chất lượng cao dành cho chim cảnh của bạn. ");
        
        if (request.getMaterial() != null) {
            description.append("Được làm từ ").append(request.getMaterial().toLowerCase()).append(" ");
        }
        
        if (request.getOrigin() != null) {
            description.append("xuất xứ từ ").append(request.getOrigin()).append(". ");
        }
        
        if (request.getUsageTarget() != null) {
            description.append("Phù hợp cho ").append(request.getUsageTarget().toLowerCase()).append(". ");
        }
        
        description.append("Sản phẩm đảm bảo an toàn và dinh dưỡng cho thú cưng của bạn. ");
        
        if (request.getWeight() != null) {
            description.append("Trọng lượng: ").append(request.getWeight()).append("kg. ");
        }
        
        if (request.getColor() != null) {
            description.append("Màu sắc: ").append(request.getColor().toLowerCase()).append(". ");
        }
        
        description.append("Hãy mua ngay để mang lại trải nghiệm tốt nhất cho chim cảnh của bạn!");
        
        return description.toString();
    }
}