package org.example.flyora_backend.service;

import org.example.flyora_backend.DTOs.ProductListDTO;
import java.util.List;

public interface HybridSearchService {
    List<ProductListDTO> hybridSearch(String query);
}
