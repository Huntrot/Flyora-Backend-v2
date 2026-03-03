package org.example.flyora_backend.repository;

import java.util.Optional;
import org.example.flyora_backend.model.FoodDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodDetailRepository extends JpaRepository<FoodDetail, Integer> {
    Optional<FoodDetail> findByProductId(Integer productId);

    @Query("SELECT MAX(f.id) FROM FoodDetail f")
    Optional<Integer> findMaxId();
}
