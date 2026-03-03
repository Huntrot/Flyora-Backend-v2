// src/main/java/org/example/flyora_backend/repository/OrderItemRepository.java
package org.example.flyora_backend.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.example.flyora_backend.model.OrderItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    @Query("SELECT MAX(oi.id) FROM OrderItem oi")
    Optional<Integer> findMaxId();

    @Query("SELECT oi.product.id AS productId, SUM(oi.quantity) AS totalSold " +
            "FROM OrderItem oi WHERE oi.product.id IN :productIds GROUP BY oi.product.id")
    List<Object[]> findSalesGroupedByProductId(@Param("productIds") List<Integer> productIds);

    default Map<Integer, Integer> findTotalSalesForProducts(List<Integer> productIds) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Object[] row : findSalesGroupedByProductId(productIds)) {
            Integer productId = (Integer) row[0];
            Long totalSold = (Long) row[1];
            result.put(productId, totalSold.intValue());
        }
        return result;
    }

    boolean existsByProductId(Integer productId);

    void deleteAllByProductId(Integer productId);
    
    void deleteByOrderId(Integer orderId);
}
