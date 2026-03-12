package org.example.flyora_backend.controller;

// import java.util.List;
// import java.util.Map;

// import org.example.flyora_backend.DTOs.SystemLogDTO;
// import org.example.flyora_backend.service.SystemLogService;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.tags.Tag;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/v1/system-logs")
// @Tag(name = "System Logs", description = "API để ghi và xem lịch sử hoạt động của hệ thống")
// @RequiredArgsConstructor
public class SystemLogController {

    // private final SystemLogService systemLogService;

    // @PostMapping("/log")
    // @Operation(
    //     summary = "Ghi một hành động vào nhật ký hệ thống",
    //     description = """
    //         Dùng để ghi lại các hành động quan trọng do Admin thực hiện.
    //         **API này thường được gọi từ bên trong các service khác, không phải từ frontend.**
            
    //         **Quyền truy cập:** Chỉ dành cho Admin.
            
    //         **Body yêu cầu (SystemLogDTO):**
    //         - `adminId` (integer): ID của Admin thực hiện hành động.
    //         - `action` (string): Mô tả hành động (ví dụ: "Phê duyệt tài khoản ShopOwner #123").
            
    //         **Trả về:** Thông báo thành công.
    //         """
    // )
    // public ResponseEntity<?> createLog(@RequestBody SystemLogDTO logDTO) {
    //     try {
    //         systemLogService.logAction(logDTO.getAdminId(), logDTO.getAction());
    //         return ResponseEntity.ok(Map.of("message", "Ghi nhận hành động thành công."));
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    //     }
    // }

    // @GetMapping
    // @Operation(
    //     summary = "Xem toàn bộ nhật ký hệ thống",
    //     description = """
    //         Lấy danh sách tất cả các hành động đã được ghi lại trong hệ thống, sắp xếp theo thứ tự mới nhất.
            
    //         **Quyền truy cập:** Chỉ dành cho Admin.
            
    //         **Trả về:** Một danh sách các đối tượng `SystemLogDTO`, mỗi đối tượng bao gồm ID log, ID admin, tên admin, hành động và thời gian.
    //         """
    // )
    // public ResponseEntity<List<SystemLogDTO>> getAllLogs() {
    //     return ResponseEntity.ok(systemLogService.getAllSystemLogs());
    // }
}