package org.example.flyora_backend.controller;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.flyora_backend.DTOs.AccessLogDTO;
import org.example.flyora_backend.DTOs.AccountDTO;
import org.example.flyora_backend.DTOs.CreateNewsDTO;
import org.example.flyora_backend.DTOs.UserDTO;
import org.example.flyora_backend.model.Account;
import org.example.flyora_backend.repository.AccessLogRepository;
import org.example.flyora_backend.repository.AccountRepository;
import org.example.flyora_backend.service.AccessLogService;
import org.example.flyora_backend.service.AccountService;
import org.example.flyora_backend.service.InfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@Tag(name = "Admin Services")
public class AdminController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccessLogRepository accessLogRepository;

    @Autowired
    private AccessLogService accessLogService;

    @Autowired
    private InfoService infoService;

    private void verifyAdmin(Integer requestAccountId) {
        Optional<Account> optionalAcc = accountRepository.findById(requestAccountId);
        if (optionalAcc.isPresent()) {
            Account acc = optionalAcc.get();
            if (!acc.getRole().getName().equalsIgnoreCase("Admin")) {
                throw new RuntimeException("Access denied");
            }
        }
    }

    @PostMapping
    @Operation(summary = "Tạo tài khoản mới", description = """
                Tạo mới tài khoản (chỉ dành cho Admin).

                ✅ Trường yêu cầu trong body (AccountDTO):
                - username (String)
                - password (String)
                - phone (String)
                - roleId (Integer): 1=ADMIN, 2=SHOPOWNER, 3=SALESSTAFF, 4=CUSTOMER
                - approvedBy (Integer): ID của admin duyệt

                📌 `requesterId` là ID của tài khoản gửi request, dùng để xác thực quyền admin.

                🔁 Trả về: Account đã tạo nếu thành công.
            """)
    public ResponseEntity<?> createAccount(@RequestBody AccountDTO dto, @RequestParam Integer requesterId) {
        verifyAdmin(requesterId);
        accessLogService.logAction(requesterId, "Tạo tài khoản mới");
        Account account = accountService.createAccount(dto);
        return ResponseEntity.ok(new UserDTO(account));
    }

    @GetMapping
    @Operation(summary = "Xem danh sách tất cả tài khoản", description = """
                Trả về danh sách tất cả tài khoản hiện có trong hệ thống (chỉ dành cho Admin).

                📌 `requesterId` là ID của tài khoản gửi request, dùng để xác thực quyền admin.

                🔁 Trả về: Danh sách tất cả Account.
            """)
    public ResponseEntity<?> getAllAccounts(@RequestParam Integer requesterId) {
        verifyAdmin(requesterId);
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tài khoản", description = """
                Cập nhật thông tin tài khoản theo ID (chỉ dành cho Admin).

                ✅ Trường yêu cầu trong body (AccountDTO):
                - username (String)
                - password (String)
                - email (String)
                - phone (String)
                - roleId (Integer)
                - approvedBy (Integer)
                - isActive (Boolean)
                - isApproved (Boolean)

                📌 `requesterId` là ID của tài khoản gửi request, dùng để xác thực quyền admin.

                🔁 Trả về: Account đã cập nhật nếu thành công.
            """)
    public ResponseEntity<?> updateAccount(@PathVariable Integer id, @RequestBody AccountDTO dto,
            @RequestParam Integer requesterId) {
        verifyAdmin(requesterId);
        accessLogService.logAction(requesterId, "Cập nhật tài khoản");
        Account account = accountService.updateAccount(id, dto);
        return ResponseEntity.ok(new UserDTO(account));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tài khoản", description = """
                Xóa tài khoản theo ID (chỉ dành cho Admin).

                📌 `requesterId` là ID của tài khoản gửi request, dùng để xác thực quyền admin.

                🔁 Trả về: Thông báo xóa thành công nếu thực hiện được.
            """)
    public ResponseEntity<?> deleteAccount(@PathVariable Integer id, @RequestParam Integer requesterId) {
        verifyAdmin(requesterId);
        accountService.deleteAccount(id);
        accessLogService.logAction(requesterId, "Xóa tài khoản");
        return ResponseEntity.ok("Tài khoản đã được xóa thành công.");
    }

    @PutMapping("/{id}/activate")
    @Operation(summary = "Kích hoạt tài khoản", description = """
                Kích hoạt tài khoản (chỉ dành cho Admin).

                📌 `requesterId` là ID của tài khoản gửi request, dùng để xác thực quyền admin.

                🔁 Trả về: Account đã được kích hoạt.
            """)
    public ResponseEntity<?> activateAccount(@PathVariable Integer id, @RequestParam Integer requesterId) {
        verifyAdmin(requesterId);
        accessLogService.logAction(requesterId, "Kích hoạt");
        Account account = accountService.setActiveStatus(id, true);
        return ResponseEntity.ok(new UserDTO(account));
    }

    @PutMapping("/{id}/deactivate")
    @Operation(summary = "Hủy kích hoạt tài khoản", description = """
                Hủy kích hoạt tài khoản (chỉ dành cho Admin).

                📌 `requesterId` là ID của tài khoản gửi request, dùng để xác thực quyền admin.

                🔁 Trả về: Account đã được hủy kích hoạt.
            """)
    public ResponseEntity<?> deactivateAccount(@PathVariable Integer id, @RequestParam Integer requesterId) {
        verifyAdmin(requesterId);
        accessLogService.logAction(requesterId, "Hủy kích hoạt");
        Account account = accountService.setActiveStatus(id, false);
        return ResponseEntity.ok(new UserDTO(account));
    }

    @GetMapping("/logs")
    @Operation(summary = "Xem lịch sử hoạt động người dùng", description = """
                Trả về danh sách các hoạt động truy cập của tất cả tài khoản (chỉ dành cho Admin).

                📌 `requesterId` là ID tài khoản yêu cầu (Admin).

                🔁 Trả về: Danh sách AccessLogDTO.
            """)
    public ResponseEntity<?> getAccessLogs(@RequestParam Integer requesterId) {
        verifyAdmin(requesterId);

        List<AccessLogDTO> logs = accessLogRepository.findAll().stream()
                .map(log -> {
                    AccessLogDTO dto = new AccessLogDTO();
                    dto.setAccountId(log.getAccount().getId());
                    dto.setUsername(log.getAccount().getUsername());
                    dto.setAction(log.getAction());
                    dto.setTimestamp(log.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(logs);
    }

    @PostMapping("/news")
    @Operation(summary = "Tạo bản tin mới", description = """
                Tạo bài viết mới (NewsArticle) từ URL và tiêu đề (chỉ dành cho Admin).

                📌 Trường yêu cầu trong body:
                - title (String): Tiêu đề
                - url (String): Đường dẫn bài viết (có thể crawl nội dung nếu cần)

                📌 `requesterId`: ID tài khoản gọi request để xác minh quyền Admin.

                🔁 Trả về: Bản tin vừa tạo.
            """)
    public ResponseEntity<?> createNews(@RequestBody CreateNewsDTO dto, @RequestParam Integer requesterId) {
        verifyAdmin(requesterId);
        accessLogService.logAction(requesterId, "Tạo bản tin: " + dto.getTitle());
        return ResponseEntity.ok(infoService.createNewsArticle(dto));
    }

}
