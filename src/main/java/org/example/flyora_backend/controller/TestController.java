package org.example.flyora_backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    private final PayOS payOS = new PayOS(
            "3dc81b4b-255a-416a-a007-881821442b20",
            "4370621a-be81-4a53-bdda-6046763f433a",
            "29fbe7cb4109b098634b0a3dd62fba43abfda3a8460431ec6daa08c8e56bc27a"
    );

    @GetMapping("/check-payos-key")
    public ResponseEntity<?> checkPayosKey() {

        try {

            long orderCode = System.currentTimeMillis();

            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(5000L)
                    .description("test-key")
                    .returnUrl("https://example.com/success")
                    .cancelUrl("https://example.com/cancel")
                    .build();

            log.info("Sending payment request: {}", request);

            CreatePaymentLinkResponse response = payOS.paymentRequests().create(request);

            log.info("Checkout URL: {}", response.getCheckoutUrl());

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "PayOS key hợp lệ",
                            "checkoutUrl", response.getCheckoutUrl(),
                            "orderCode", orderCode
                    )
            );

        } catch (Exception e) {

            log.error("PayOS error", e);

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", "PayOS key không hợp lệ",
                            "error", e.getMessage()
                    )
            );
        }
    }
}