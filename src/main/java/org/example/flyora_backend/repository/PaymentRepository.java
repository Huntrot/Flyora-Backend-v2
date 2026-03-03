package org.example.flyora_backend.repository;

import java.util.Optional;

import org.example.flyora_backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("SELECT MAX(p.id) FROM Payment p")
    Optional<Integer> findMaxId();

    void deleteByCustomerId(Integer customerId);

}