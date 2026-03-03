package org.example.flyora_backend.service;

import java.util.List;

import org.example.flyora_backend.DTOs.ChatBotDTO;
import org.example.flyora_backend.model.ChatBot;

public interface ChatBotService {
    void sendMessage(ChatBotDTO dto);
    List<ChatBot> getMessages(Integer customerId);
}