package org.example.flyora_backend.repository;

import java.util.Optional;

import org.example.flyora_backend.model.DeliveryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Integer>{
    @Query("SELECT MAX(f.id) FROM DeliveryNote f")
    Optional<Integer> findMaxId();

    Optional<DeliveryNote> findByTrackingNumber(String trackingNumber);

    void deleteByOrderId(Integer orderId);
}
