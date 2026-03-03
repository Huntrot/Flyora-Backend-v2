package org.example.flyora_backend.service;

import org.example.flyora_backend.DTOs.CreateProductDTO;

public interface AIService {
    String generateProductDescription(CreateProductDTO request);
}