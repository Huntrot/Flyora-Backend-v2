package org.example.flyora_backend.repository;

import java.util.Optional;
import org.example.flyora_backend.model.ToyDetail;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ToyDetailRepository extends JpaRepository<ToyDetail, Integer> {
    Optional<ToyDetail> findByProductId(Integer productId);

    @Query("SELECT MAX(t.id) FROM ToyDetail t")
    Optional<Integer> findMaxId();
}