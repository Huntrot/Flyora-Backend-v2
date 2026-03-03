package org.example.flyora_backend.repository;

import java.util.List;
import java.util.Optional;

import org.example.flyora_backend.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Integer> {
    List<ProductReview> findByProductId(Integer productId);

    @Query("SELECT MAX(r.id) FROM ProductReview r")
    Optional<Integer> findMaxId();

    void deleteAllByProductId(Integer productId);

    @Query("SELECT r FROM ProductReview r " +
           "JOIN FETCH r.customer c " +
           "JOIN r.product p " + // Join bình thường, không FETCH product
           "WHERE p.id = :productId")
    List<ProductReview> findByProductIdWithCustomer(@Param("productId") Integer productId);

    void deleteByCustomerId(Integer customerId);
}