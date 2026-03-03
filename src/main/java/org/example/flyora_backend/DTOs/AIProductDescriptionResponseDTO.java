package org.example.flyora_backend.DTOs;

import lombok.Data;

@Data
public class AIProductDescriptionResponseDTO {
    private String description;
    private boolean success;
    private String message;
    
    public AIProductDescriptionResponseDTO(String description, boolean success, String message) {
        this.description = description;
        this.success = success;
        this.message = message;
    }
}