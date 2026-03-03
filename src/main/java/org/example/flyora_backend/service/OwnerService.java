package org.example.flyora_backend.service;

import java.util.List;

import org.example.flyora_backend.DTOs.CreateProductDTO;
import org.example.flyora_backend.DTOs.OwnerProductListDTO;
import org.example.flyora_backend.DTOs.TopProductDTO;
import org.example.flyora_backend.model.Product;

public interface OwnerService {
    List<TopProductDTO> getTopSellingProducts();

    public Product createProduct(CreateProductDTO dto, Integer accountId);

    List<OwnerProductListDTO> getAllProductsByOwner(int accountId);

    public Product updateProduct(Integer productId, CreateProductDTO dto, Integer accountId);

    public void deleteProduct(Integer productId, Integer accountId);
    
    List<OwnerProductListDTO> searchProductsByOwner(String keyword);
}
