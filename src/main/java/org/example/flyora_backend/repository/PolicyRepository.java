package org.example.flyora_backend.repository;

import org.example.flyora_backend.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE Policy p SET p.updatedBy = NULL WHERE p.updatedBy.id = :accountId")
    void clearUpdatedBy(Integer accountId);
}