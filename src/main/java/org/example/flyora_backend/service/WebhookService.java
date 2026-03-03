package org.example.flyora_backend.service;

import java.util.Map;

import org.example.flyora_backend.DTOs.EmailDTO;
import org.example.flyora_backend.model.DeliveryNote;
import org.example.flyora_backend.model.Order;
import org.example.flyora_backend.repository.DeliveryNoteRepository;
import org.example.flyora_backend.repository.OrderRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public void processWebhook(Map<String, Object> payload) {
        String orderCode = (String) payload.get("OrderCode");
        String status = (String) payload.get("Status");
        if(orderCode == null || status == null) {
            System.out.println("Invalid webhook payload: missing OrderCode or Status");
            return;
        }
        DeliveryNote deliveryNote = deliveryNoteRepository.findByTrackingNumber(orderCode).orElseThrow(() -> new RuntimeException("Tracking number not found"));

        Order order = deliveryNote.getOrder();
        String newStatus = mapGHNStatus(status);

        if (!order.getStatus().equals(newStatus)) {
            order.setStatus(newStatus);
            orderRepository.save(order);

            sendStatusUpdateEmail(order);
        }

    }

    private String mapGHNStatus(String ghnStatus) {
        return switch (ghnStatus.toLowerCase()) {
            case "delivered" -> "DELIVERED";
            case "cancel", "return" -> "CANCELLED";
            default -> "Shipping";
        };
    }

    private void sendStatusUpdateEmail(Order order) {

        String email = order.getCustomer().getAccount().getEmail();

        String subject = "Flyora - Cập nhật đơn hàng #" + order.getOrderCode();

        String content = """
                <h2>Đơn hàng của bạn đã được cập nhật 🐦</h2>
                <p>Mã đơn hàng: <b>%s</b></p>
                <p>Trạng thái mới: <b>%s</b></p>
                """.formatted(order.getOrderCode(), order.getStatus());

        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setTo(email);
        emailDTO.setSubject(subject);
        emailDTO.setContent(content);

        emailService.sendEmail(emailDTO);
    }
}
