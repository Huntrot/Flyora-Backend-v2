// Modified: ProductService.java
package org.example.flyora_backend.service;

import java.util.List;

import org.example.flyora_backend.DTOs.ProductBestSellerDTO;
import org.example.flyora_backend.DTOs.ProductDetailDTO;
import org.example.flyora_backend.DTOs.ProductFilterDTO;
import org.example.flyora_backend.DTOs.ProductListDTO;
import org.example.flyora_backend.model.Product;

public interface ProductService {
    List<ProductListDTO> filterProducts(ProductFilterDTO filter);

    ProductDetailDTO getProductDetail(Integer id);

    List<ProductBestSellerDTO> getTop15BestSellers();

    List<ProductListDTO> searchByName(String name);

    List<ProductListDTO> getProductByStatus();

    Product addProduct(Product product);

    boolean deleteProductById(int id);
}

