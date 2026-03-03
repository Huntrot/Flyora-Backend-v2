package org.example.flyora_backend.repository;

import org.example.flyora_backend.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

public interface FaqRepository extends JpaRepository<Faq, Integer> {
    @Modifying
    @Transactional
    @Query("UPDATE Faq f SET f.updatedBy = NULL WHERE f.updatedBy.id = :accountId")
    void clearUpdatedBy(Integer accountId);
}