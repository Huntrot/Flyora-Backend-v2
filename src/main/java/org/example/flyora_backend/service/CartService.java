package org.example.flyora_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.example.flyora_backend.DTOs.CartItemDTO;
import org.example.flyora_backend.DTOs.CartRequestDTO;
import org.example.flyora_backend.model.FoodDetail;
import org.example.flyora_backend.model.FurnitureDetail;
import org.example.flyora_backend.model.Product;
import org.example.flyora_backend.model.ToyDetail;
import org.example.flyora_backend.repository.FoodDetailRepository;
import org.example.flyora_backend.repository.FurnitureDetailRepository;
import org.example.flyora_backend.repository.ProductRepository;
import org.example.flyora_backend.repository.ToyDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FoodDetailRepository foodDetailRepository;

    @Autowired
    private ToyDetailRepository toyDetailRepository;

    @Autowired
    private FurnitureDetailRepository furnitureDetailRepository;

    public List<CartItemDTO> getCartItems(List<CartRequestDTO> cartRequests) {
        List<CartItemDTO> result = new ArrayList<>();

        for (CartRequestDTO request : cartRequests) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            String imageUrl = getImageUrlByCategory(product);
            result.add(CartItemDTO.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .imageUrl(imageUrl)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build());
        }

        return result;
    }

    public CartItemDTO updateItem(Integer productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        String imageUrl = getImageUrlByCategory(product);
        return CartItemDTO.builder()
                .productId(product.getId())
                .name(product.getName())
                .imageUrl(imageUrl)
                .quantity(quantity)
                .price(product.getPrice())
                .build();
    }

    private String getImageUrlByCategory(Product product) {
        Integer categoryId = product.getCategory().getId();
        return switch (categoryId) {
            case 1 -> foodDetailRepository.findByProductId(product.getId())
                    .map(FoodDetail::getImageUrl).orElse(null);
            case 2 -> toyDetailRepository.findByProductId(product.getId())
                    .map(ToyDetail::getImageUrl).orElse(null);
            case 3 -> furnitureDetailRepository.findByProductId(product.getId())
                    .map(FurnitureDetail::getImageUrl).orElse(null);
            default -> null;
        };
    }
}
