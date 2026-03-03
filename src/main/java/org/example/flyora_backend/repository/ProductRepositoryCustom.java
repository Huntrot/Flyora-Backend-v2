package org.example.flyora_backend.repository;

import java.math.BigDecimal;
import java.util.List;

import org.example.flyora_backend.DTOs.OwnerProductListDTO;
import org.example.flyora_backend.DTOs.ProductBestSellerDTO;
import org.example.flyora_backend.DTOs.ProductListDTO;
import org.example.flyora_backend.DTOs.TopProductDTO;

public interface ProductRepositoryCustom {
    List<ProductListDTO> filterProducts(
            String name,
            Integer categoryId,
            Integer birdTypeId,
            BigDecimal minPrice,
            BigDecimal maxPrice);

    List<ProductBestSellerDTO> findTop15BestSellers();

    List<ProductListDTO> searchByName(String name);

    List<TopProductDTO> findTopSellingProductsByShopOwner();

    List<OwnerProductListDTO> findAllByShopOwnerIdOrderByIdAsc(int ownerId);

}
