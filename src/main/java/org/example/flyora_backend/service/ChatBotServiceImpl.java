package org.example.flyora_backend.service;

import java.time.Instant;
import java.util.List;

import org.example.flyora_backend.DTOs.ChatBotDTO;
import org.example.flyora_backend.model.ChatBot;
import org.example.flyora_backend.model.Customer;
import org.example.flyora_backend.repository.ChatBotRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatBotServiceImpl implements ChatBotService {

    private final ChatBotRepository chatBotRepository;

    @Override
    public void sendMessage(ChatBotDTO dto) {
        ChatBot chat = new ChatBot();
        Customer customer = new Customer();
        customer.setId(dto.customerId());
        chat.setCustomer(customer);

        chat.setMessage(dto.message());
        chat.setResponse(null); // chưa phản hồi
        chat.setCreatedAt(Instant.now());

        chatBotRepository.save(chat);
    }

    @Override
    public List<ChatBot> getMessages(Integer customerId) {
        return chatBotRepository.findByCustomer_IdOrderByCreatedAtDesc(customerId);
    }
}
