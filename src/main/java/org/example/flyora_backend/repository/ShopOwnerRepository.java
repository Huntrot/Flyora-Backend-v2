package org.example.flyora_backend.repository;

import java.util.Optional;
import org.example.flyora_backend.model.ShopOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShopOwnerRepository extends JpaRepository<ShopOwner, Integer> {
    Optional<ShopOwner> findByAccountId(Integer accountId);

    @Query("SELECT MAX(a.id) FROM ShopOwner a")
    Optional<Integer> findMaxId();

    void deleteByAccountId(Integer accountId);
}
