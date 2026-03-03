package org.example.flyora_backend.repository;

import java.util.List;

import org.example.flyora_backend.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    List<Promotion> findByCustomer_Id(Integer customerId);

    void deleteAllByProductId(Integer productId);

    void deleteByCustomerId(Integer customerId);

}