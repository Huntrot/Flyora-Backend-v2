package org.example.flyora_backend.repository;

import java.util.List;
import java.util.Optional;

import org.example.flyora_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Integer customerId);

    @Query("SELECT MAX(o.id) FROM Order o")
    Optional<Integer> findMaxId();

    // Giữ lại code từ cả hai nhánh
    @Query("SELECT o.id FROM Order o WHERE o.customer.id = :customerId")
    List<Integer> findIdsByCustomerId(@Param("customerId") Integer customerId);

    void deleteByCustomerId(Integer customerId);

    Optional<Order> findByOrderCode(String orderCode);

}
