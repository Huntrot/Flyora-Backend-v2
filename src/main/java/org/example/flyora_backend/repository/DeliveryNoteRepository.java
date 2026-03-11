package org.example.flyora_backend.repository;

import java.util.List;
import java.util.Optional;

import org.example.flyora_backend.model.DeliveryNote;
import org.example.flyora_backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryNoteRepository extends JpaRepository<DeliveryNote, Integer>{
    @Query("SELECT MAX(f.id) FROM DeliveryNote f")
    Optional<Integer> findMaxId();

    Optional<DeliveryNote> findByTrackingNumber(String trackingNumber);

    void deleteByOrderId(Integer orderId);

    List<DeliveryNote> findByCompletedFalse();
    
    Optional<DeliveryNote> findByOrder(Order order);

    @Query("SELECT dn FROM DeliveryNote dn JOIN FETCH dn.order WHERE dn.completed = false")
    List<DeliveryNote> findIncompleteWithOrder();
}
