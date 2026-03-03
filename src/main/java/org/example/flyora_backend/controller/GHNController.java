package org.example.flyora_backend.controller;

import java.util.List;
import java.util.Map;

import org.example.flyora_backend.DTOs.CalculateFeeRequestDTO;
import org.example.flyora_backend.DTOs.CreateOrderRequestDTO;
import org.example.flyora_backend.DTOs.DistrictDTO;
import org.example.flyora_backend.DTOs.ProvinceDTO;
import org.example.flyora_backend.DTOs.WardDTO;
import org.example.flyora_backend.model.Account;
import org.example.flyora_backend.model.DeliveryNote;
import org.example.flyora_backend.repository.AccountRepository;
import org.example.flyora_backend.repository.DeliveryNoteRepository;
import org.example.flyora_backend.service.GHNService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/shipping-utils")
@Tag(name = "Shipping Utilities (GHN)", description = "Các API tiện ích để lấy dữ liệu vận chuyển từ GHN (tỉnh thành, phí ship,...)")
public class GHNController {

    @Autowired
    private GHNService ghnService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DeliveryNoteRepository deliveryNoteRepository; // Inject repo này để kiểm tra bảo mật

    private Account verifyAccess(Integer requesterId) {
        Account acc = accountRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        if (!acc.getIsActive() || !acc.getIsApproved()) {
            throw new RuntimeException("Tài khoản bị khóa hoặc chưa duyệt");
        }
        return acc;
    }

    @GetMapping("/provinces")
    @Operation(summary = "Lấy danh sách Tỉnh/Thành", description = """
            Lấy danh sách tất cả các tỉnh/thành phố của Việt Nam để hiển thị cho người dùng chọn.

            🔑 **Quyền truy cập:** Bất kỳ tài khoản nào đã được duyệt.
            - **`requesterId`** (param): ID của tài khoản thực hiện yêu cầu.
            """)
    public ResponseEntity<List<ProvinceDTO>> getProvinces(@RequestParam Integer requesterId) {
        verifyAccess(requesterId);
        return ResponseEntity.ok(ghnService.getProvinces());
    }

    @GetMapping("/districts")
    @Operation(summary = "Lấy danh sách Quận/Huyện theo Tỉnh", description = """
            Lấy danh sách các quận/huyện thuộc một tỉnh/thành cụ thể.

            🔑 **Quyền truy cập:** Bất kỳ tài khoản nào đã được duyệt.
            - **`requesterId`** (param): ID của tài khoản thực hiện yêu cầu.
            - **`provinceId`** (param): ID của tỉnh/thành đã chọn.
            """)
    public ResponseEntity<List<DistrictDTO>> getDistricts(
            @RequestParam Integer requesterId,
            @RequestParam int provinceId) {
        verifyAccess(requesterId);
        return ResponseEntity.ok(ghnService.getDistricts(provinceId));
    }

    @GetMapping("/wards")
    @Operation(summary = "Lấy danh sách Phường/Xã theo Quận", description = """
            Lấy danh sách các phường/xã thuộc một quận/huyện cụ thể.

            🔑 **Quyền truy cập:** Bất kỳ tài khoản nào đã được duyệt.
            - **`requesterId`** (param): ID của tài khoản thực hiện yêu cầu.
            - **`districtId`** (param): ID của quận/huyện đã chọn.
            """)
    public ResponseEntity<List<WardDTO>> getWards(
            @RequestParam Integer requesterId,
            @RequestParam int districtId) {
        verifyAccess(requesterId);
        return ResponseEntity.ok(ghnService.getWard(districtId));
    }

    @PostMapping("/calculate-fee")
    @Operation(summary = "Tính phí vận chuyển dự kiến", description = """
            Tính toán chi phí vận chuyển dự kiến trước khi khách hàng đặt hàng.
            Frontend nên gọi API này mỗi khi người dùng thay đổi địa chỉ hoặc giỏ hàng.

            🔑 **Quyền truy cập:** Bất kỳ tài khoản nào đã được duyệt.

            ✅ **Body yêu cầu (CalculateFeeRequestDTO):**
            - `to_district_id`, `to_ward_code`: Địa chỉ người nhận.
            - `weight`, `height`, `length`, `width`: Thông tin gói hàng.
            - `insurance_value`: Giá trị đơn hàng.
            - `service_id`: ID dịch vụ của GHN.
            """)
    public ResponseEntity<Map<String, Object>> calculateShippingFee(
            @RequestParam Integer requesterId,
            @RequestBody CalculateFeeRequestDTO feeRequest) {
        verifyAccess(requesterId);
        return ResponseEntity.ok(ghnService.calculateFee(feeRequest));
    }

    @PostMapping("/create-order")
    @Operation(summary = "[Hiện tại không cần dùng vì đã có bên OrderController] Tạo đơn hàng vận chuyển", description = """
            Gửi thông tin đơn hàng sang hệ thống của GHN để đăng ký một đơn vận chuyển mới.
            **Lưu ý:** Sau bước này, bạn phải lưu lại `order_code` trả về vào database của mình.

            🔑 **Quyền truy cập:** Bất kỳ tài khoản nào đã được duyệt.

            - **`requesterId`** (param): ID của tài khoản thực hiện yêu cầu.

            ✅ **Trường yêu cầu trong body (CreateOrderRequestDTO):** Xem chi tiết trong Schemas.
                **Lưu ý chỉ được sử dụng số điện thoại sau: "0942921287"

            🔁 **Trả về:** Một đối tượng JSON chứa thông tin đơn hàng đã tạo thành công, bao gồm `order_code` quan trọng.
            """)
    public ResponseEntity<Map<String, Object>> createShippingOrder(
            @RequestParam Integer requesterId,
            @RequestBody CreateOrderRequestDTO orderRequest) {

        verifyAccess(requesterId);
        Map<String, Object> ghnResponse = ghnService.createOrder(orderRequest);

        // Logic để lưu ghnResponse.get("order_code") vào database sẽ được thực hiện ở
        // đây.

        return ResponseEntity.ok(ghnResponse);
    }

    @GetMapping("/track")
    @Operation(summary = "Theo dõi trạng thái đơn hàng", description = """
            Lấy thông tin chi tiết và lịch sử trạng thái của một đơn hàng qua GHN.

            🔑 **Quyền truy cập:** Khách hàng (chỉ xem được đơn của mình) hoặc Nhân viên/Chủ shop.

            - **`requesterId`** (param): ID của tài khoản thực hiện yêu cầu.
            - **`orderCode`** (param): Mã vận đơn do GHN cung cấp (lấy từ bảng DeliveryNote).

            🔁 **Trả về:** Một đối tượng JSON chứa thông tin chi tiết của đơn hàng, bao gồm `status`, `log` (lịch sử di chuyển), `expected_delivery_time`,...
            """)
    public ResponseEntity<?> trackOrder(
            @RequestParam Integer requesterId,
            @RequestParam String orderCode) {

        Account account = verifyAccess(requesterId);

        // **Bảo mật quan trọng:** Kiểm tra quyền sở hữu đơn hàng
        DeliveryNote deliveryNote = deliveryNoteRepository.findByTrackingNumber(orderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã vận đơn này."));

        // Chỉ cho phép admin, hoặc chủ sở hữu đơn hàng xem
        boolean isOwner = deliveryNote.getOrder().getCustomer().getAccount().getId().equals(requesterId);
        boolean isAdminOrStaff = account.getRole().getName().equals("Admin")
                || account.getRole().getName().equals("ShopOwner");

        if (!isOwner && !isAdminOrStaff) {
            return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền xem đơn hàng này."));
        }

        Map<String, Object> orderDetails = ghnService.getOrderStatus(orderCode);
        return ResponseEntity.ok(orderDetails);
    }

    
}
