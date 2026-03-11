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
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GHNPollingService {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final OrderRepository orderRepository;
    private final GHNService ghnService;
    private final EmailService emailService;

    // Poll GHN every 5 minutes
    @Scheduled(fixedRate = 300000)
    public void syncShippingStatus() {

        List<DeliveryNote> notes = deliveryNoteRepository.findIncompleteWithOrder();

        for (DeliveryNote note : notes) {

            String trackingNumber = note.getTrackingNumber();

            try {

                Map<String, Object> response = ghnService.getOrderDetail(trackingNumber);

                if (response == null) {
                    System.out.println("GHN response null for tracking: " + trackingNumber);
                    continue;
                }

                String ghnStatus = (String) response.get("status");

                if (ghnStatus == null) {
                    System.out.println("GHN status null for tracking: " + trackingNumber);
                    continue;
                }

                String mappedStatus = mapStatus(ghnStatus);

                Order order = note.getOrder();

                if (order == null) {
                    System.out.println("Order null for delivery note tracking: " + trackingNumber);
                    continue;
                }

                // Update order status if changed
                if (order.getStatus() == null ||
                        !order.getStatus().equalsIgnoreCase(mappedStatus)) {

                    order.setStatus(mappedStatus);
                    orderRepository.save(order);

                    note.setStatus(ghnStatus);
                    note.setLastCheckedAt(Instant.now());

                    if (mappedStatus.equals("DELIVERED")
                            || mappedStatus.equals("CANCELLED")) {
                        note.setCompleted(true);
                    }

                    deliveryNoteRepository.save(note);

                    // Send email notification
                    emailService.sendStatusUpdateEmail(order);
                } else {

                    // Update last checked time even if status unchanged
                    note.setLastCheckedAt(Instant.now());
                    deliveryNoteRepository.save(note);
                }

            } catch (HttpClientErrorException.BadRequest ex) {

                String responseBody = ex.getResponseBodyAsString();

                if (responseBody.contains("Đơn hàng không tồn tại")) {

                    System.out.println("Tracking không tồn tại trên GHN → bỏ qua: " + trackingNumber);

                    note.setCompleted(true);
                    note.setStatus("INVALID");
                    note.setLastCheckedAt(Instant.now());

                    deliveryNoteRepository.save(note);

                } else {

                    System.out.println("GHN 400 error for " + trackingNumber + ": " + responseBody);
                }

            } catch (Exception e) {

                System.out.println("GHN polling error for " + trackingNumber + ": " + e.getMessage());
            }
        }
    }

    private String mapStatus(String ghnStatus) {

        if (ghnStatus == null) {
            return "Shipping";
        }

        switch (ghnStatus.toLowerCase()) {

            case "ready_to_pick":
            case "picking":
            case "transporting":
            case "sorting":
            case "delivering":
                return "Shipping";

            case "delivered":
                return "DELIVERED";

            case "cancel":
            case "return":
            case "returned":
            case "delivery_fail":
                return "CANCELLED";

            default:
                return "Shipping";
        }
    }
}