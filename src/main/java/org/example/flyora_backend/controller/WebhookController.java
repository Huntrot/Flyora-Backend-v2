package org.example.flyora_backend.controller;

// import java.util.Map;

// import org.example.flyora_backend.service.WebhookService;
// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v2/webhooks")
@RequiredArgsConstructor
@Tag(name = "GHN Webhook (**Hủy bỏ**)", description = "Nhận callback realtime từ GHN khi trạng thái đơn hàng thay đổi")
public class WebhookController {

    // private final WebhookService webhookService;

    // @PostMapping("/ghn")
    // @Operation(
    //     summary = "Nhận webhook cập nhật trạng thái đơn hàng từ GHN",
    //     description = """
    //         Endpoint này được GHN gọi tự động khi trạng thái đơn hàng thay đổi.

    //         📦 Body (JSON từ GHN):
    //         - OrderCode (String): Mã vận đơn GHN
    //         - Status (String): Trạng thái mới (VD: delivered, cancel, return...)
    //         - UpdatedDate (String - ISO datetime)

    //         📌 Luồng xử lý:
    //         1. Nhận payload từ GHN
    //         2. Tìm DeliveryNote theo OrderCode
    //         3. Map trạng thái GHN sang trạng thái hệ thống
    //         4. Cập nhật Order trong database
    //         5. Gửi email thông báo cho khách hàng

    //         🔁 Trả về:
    //         - message nếu xử lý thành công

    //         ⚠️ Lưu ý:
    //         - Endpoint này KHÔNG dành cho frontend gọi.
    //         - Chỉ GHN mới được phép gọi (production nên có token hoặc xác thực IP).
    //     """
    // )
    // public ResponseEntity<?> receiveWebhook(
    //         @RequestBody Map<String, Object> payload
    // ) {

    //     System.out.println("==== GHN WEBHOOK RECEIVED ====");
    //     System.out.println("Payload: " + payload);

    //     webhookService.processWebhook(payload);

    //     return ResponseEntity.ok(Map.of(
    //             "message", "Webhook received and processed successfully"
    //     ));
    // }
}