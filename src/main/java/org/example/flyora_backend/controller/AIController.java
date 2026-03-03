package org.example.flyora_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.flyora_backend.DTOs.CreateProductDTO;
import org.example.flyora_backend.DTOs.AIProductDescriptionResponseDTO;
import org.example.flyora_backend.model.response.ResponseObject;
import org.example.flyora_backend.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2")
@Tag(name = "AI Controller", description = "API cho các tính năng AI")
@CrossOrigin(origins = "*")
public class AIController {
    
    @Autowired
    private AIService aiService;
    
    @PostMapping("/generate-description")
    @Operation(summary = "Tạo mô tả sản phẩm bằng AI", 
               description = "Sử dụng Gemini AI để tạo mô tả sản phẩm dựa trên thông tin đầu vào")
    public ResponseEntity<ResponseObject> generateProductDescription(
            @RequestBody CreateProductDTO request) {
        
        try {
            String description = aiService.generateProductDescription(request);
            
            AIProductDescriptionResponseDTO response = new AIProductDescriptionResponseDTO(
                description, true, "Tạo mô tả sản phẩm thành công"
            );
            
            return ResponseEntity.ok(
                new ResponseObject(200, "Tạo mô tả sản phẩm thành công", response)
            );
            
        } catch (Exception e) {
            AIProductDescriptionResponseDTO response = new AIProductDescriptionResponseDTO(
                null, false, "Lỗi khi tạo mô tả: " + e.getMessage()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject(500, "Lỗi khi tạo mô tả sản phẩm", response));
        }
    }
}