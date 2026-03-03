        package org.example.flyora_backend.repository;

        import java.util.List;
        import java.util.Optional;

        import org.example.flyora_backend.DTOs.OwnerProductListDTO;
        import org.example.flyora_backend.DTOs.ProductListDTO;
        import org.example.flyora_backend.model.Product;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.jpa.repository.Query;
        import org.springframework.data.repository.query.Param;

        public interface ProductRepository extends JpaRepository<Product, Integer>, ProductRepositoryCustom {
        List<ProductListDTO> findAllByStatusTrue();

        @Query("SELECT MAX(p.id) FROM Product p")
        Optional<Integer> findMaxId();

        @Query("SELECT new org.example.flyora_backend.DTOs.OwnerProductListDTO(" +
                "p.id, p.name, p.price, p.stock, " +
                "CASE WHEN p.stock > 0 THEN 'Còn hàng' ELSE 'Hết hàng' END, " +
                "COALESCE(fd.imageUrl, td.imageUrl, fud.imageUrl)) " +
                "FROM Product p " +
                "LEFT JOIN FoodDetail fd ON p.id = fd.product.id " +
                "LEFT JOIN ToyDetail td ON p.id = td.product.id " +
                "LEFT JOIN FurnitureDetail fud ON p.id = fud.product.id " +
                "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +            
                "ORDER BY p.id ASC")
        List<OwnerProductListDTO> searchProductsByShopOwner(
                @Param("keyword") String keyword);
        }
