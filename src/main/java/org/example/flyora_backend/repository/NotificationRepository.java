package org.example.flyora_backend.repository;

import org.example.flyora_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByRecipient_IdOrderByCreatedAtDesc(Integer recipientId);

    void deleteByRecipientId(Integer recipientId);
}