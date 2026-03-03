package org.example.flyora_backend.repository;

import java.util.List;

import org.example.flyora_backend.model.ChatBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatBotRepository extends JpaRepository<ChatBot, Integer> {
    List<ChatBot> findByCustomer_IdOrderByCreatedAtDesc(Integer customerId);

    void deleteByCustomerId(Integer customerId);

}
