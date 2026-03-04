package org.example.flyora_backend.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.example.flyora_backend.model.DeliveryNote;
import org.example.flyora_backend.model.Order;
import org.example.flyora_backend.repository.DeliveryNoteRepository;
import org.example.flyora_backend.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GHNPollingService {
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final OrderRepository orderRepository;
    private final GHNService ghnService;
    private final EmailService emailService;
    
    @Scheduled(fixedRate = 300000) // 5 phút
    @Transactional
    public void syncShippingStatus() {

        List<DeliveryNote> notes =
                deliveryNoteRepository.findByCompletedFalse();

        for (DeliveryNote note : notes) {

            String trackingNumber = note.getTrackingNumber();

            try {
                Map<String, Object> response =
                        ghnService.getOrderDetail(trackingNumber);

                String ghnStatus =
                        (String) response.get("status");

                String mappedStatus =
                        mapStatus(ghnStatus);

                Order order = note.getOrder();

                if (!order.getStatus().equalsIgnoreCase(mappedStatus)) {

                    order.setStatus(mappedStatus);
                    orderRepository.save(order);

                    note.setStatus(ghnStatus);
                    note.setLastCheckedAt(Instant.now());

                    if (mappedStatus.equals("DELIVERED")
                        || mappedStatus.equals("CANCELLED")) {
                        note.setCompleted(true);
                    }

                    deliveryNoteRepository.save(note);

                    emailService.sendStatusUpdateEmail(order);
                }

            } catch (HttpClientErrorException.BadRequest ex) {

                String responseBody = ex.getResponseBodyAsString();

                if (responseBody.contains("Đơn hàng không tồn tại")) {

                    System.out.println("Tracking không tồn tại trên GHN → bỏ qua: "
                            + trackingNumber);

                    note.setCompleted(true);   // 👈 QUAN TRỌNG
                    note.setStatus("INVALID");
                    note.setLastCheckedAt(Instant.now());

                    deliveryNoteRepository.save(note);

                } else {
                    System.out.println("GHN 400 error khác: "
                            + responseBody);
                }

            } catch (Exception e) {

                System.out.println("GHN polling error: "
                        + e.getMessage());
            }
        }
    }

    private String mapStatus(String ghnStatus) {
        return switch (ghnStatus.toLowerCase()) {
            case "ready_to_pick", "picking", "transporting" -> "Shipping";
            case "delivered" -> "DELIVERED";
            case "cancel", "return" -> "CANCELLED";
            default -> "Shipping";
        };
    }
}