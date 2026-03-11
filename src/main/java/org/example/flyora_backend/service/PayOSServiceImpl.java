package org.example.flyora_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.flyora_backend.DTOs.WebhookType;
import org.example.flyora_backend.DTOs.WebhookURL;
import org.example.flyora_backend.model.Order;

import org.example.flyora_backend.repository.OrderRepository;

import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.PaymentData;
import vn.payos.type.WebhookData;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PayOSServiceImpl implements PayOSService {

    private final PayOS payOS;
    private final OrderRepository orderRepository;


    @Override
    public Map<String, String> createPaymentLink(int orderId, int amount) {
        // ❌ Đừng tạo orderCode mới ở đây
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String orderCode = order.getOrderCode(); // ✅ Lấy từ DB

        try {
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(Long.parseLong(orderCode)) // phải là Long hợp lệ
                    .amount(amount)
                    .description("DH" + orderCode)

                    .returnUrl("https://flyora-frontend.vercel.app/success-payment") 

                    .cancelUrl("https://flyora-frontend.vercel.app/cancel-payment") 
                    .build();

            CheckoutResponseData response = payOS.createPaymentLink(paymentData);

            log.info("Tạo link thanh toán thành công: {}", response.getCheckoutUrl());

            return Map.of(
                    "paymentUrl", response.getCheckoutUrl(),
                    "orderCode", orderCode);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo link thanh toán: " + e.getMessage(), e);
        }
    }

    @Override
    public void handlePaymentWebhook(WebhookType webhookData) {

        log.info("📩 Dữ liệu webhook: {}", webhookData);

        WebhookData data = webhookData.getData();
        if (data == null || data.getOrderCode() == 0) {
            log.error("❌ Thiếu orderCode trong webhook");

            return;
        }

        String orderCode = String.valueOf(data.getOrderCode());

        String statusCode = webhookData.getCode();
        boolean success = webhookData.isSuccess();

        try {
            Order order = orderRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("❌ Không tìm thấy đơn hàng với orderCode: " + orderCode));

            if (success || "00".equals(statusCode)) {
                if (!"PAID".equalsIgnoreCase(order.getStatus())) {
                    order.setStatus("PAID");
                    orderRepository.save(order);
                    log.info("✅ Cập nhật trạng thái đơn hàng [{}] => PAID", orderCode);
                } else {
                    log.info("ℹ️ Đơn hàng [{}] đã ở trạng thái PAID", orderCode);
                }
            } else {
                // Nếu thanh toán không thành công
                if (!"PAID".equalsIgnoreCase(order.getStatus())) {
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);
                    log.warn("⚠️ Cập nhật trạng thái đơn hàng [{}] => CANCELLED do thanh toán thất bại", orderCode);
                }
            }

        } catch (Exception e) {
            log.error("❌ Lỗi cập nhật đơn hàng theo webhook: {}", e.getMessage(), e);
        }

    }

    

    @Override
    public String confirmWebhook(WebhookURL body) {
        log.info("🔔 Webhook xác nhận từ: {}", body.getWebhookUrl());
        return "Webhook confirmed: " + body.getWebhookUrl();
    }
}
