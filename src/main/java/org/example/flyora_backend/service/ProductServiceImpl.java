package org.example.flyora_backend.service;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.example.flyora_backend.DTOs.ProductBestSellerDTO;
import org.example.flyora_backend.DTOs.ProductDetailDTO;
import org.example.flyora_backend.DTOs.ProductFilterDTO;
import org.example.flyora_backend.DTOs.ProductListDTO;
import org.example.flyora_backend.model.Product;
import org.example.flyora_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public List<ProductListDTO> filterProducts(ProductFilterDTO filter) {
        // BỎ ĐI VIỆC TẠO Pageable
        return productRepository.filterProducts(
                filter.getName(),
                filter.getCategoryId(),
                filter.getBirdTypeId(),
                filter.getMinPrice(),
                filter.getMaxPrice());
    }

    @Override
    public ProductDetailDTO getProductDetail(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        String imageUrl = null;
        String categoryName = product.getCategory().getName();

        switch (categoryName) {
            case "FOODS" -> imageUrl = product.getFoodDetail() != null ? product.getFoodDetail().getImageUrl() : null;
            case "TOYS" -> imageUrl = product.getToyDetail() != null ? product.getToyDetail().getImageUrl() : null;
            case "FURNITURE" ->
                imageUrl = product.getFurnitureDetail() != null ? product.getFurnitureDetail().getImageUrl() : null;
        }

        return new ProductDetailDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                categoryName,
                product.getBirdType().getName(),
                imageUrl);
    }

    @Override
    public List<ProductBestSellerDTO> getTop15BestSellers() {
        return productRepository.findTop15BestSellers();
    }

    @Override
    public List<ProductListDTO> searchByName(String name) {
        return productRepository.searchByName(name);
    }

    @Override
    public List<ProductListDTO> getProductByStatus() {
        return productRepository.findAllByStatusTrue();
    }

    @Override
    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public boolean deleteProductById(int id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
