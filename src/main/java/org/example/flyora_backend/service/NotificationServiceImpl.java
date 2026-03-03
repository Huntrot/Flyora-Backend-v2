// NotificationServiceImpl.java
package org.example.flyora_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.flyora_backend.DTOs.NotificationDTO;
import org.example.flyora_backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationDTO> getNotifications(Integer recipientId) {        

        return notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(recipientId)
            .stream()
            .map(n -> new NotificationDTO(
                n.getId(),
                n.getContent(),
                n.getCreatedAt().toString() // hoặc format(n.getCreatedAt()) nếu cần chuỗi đẹp hơn
            ))
            .collect(Collectors.toList());
    }
}