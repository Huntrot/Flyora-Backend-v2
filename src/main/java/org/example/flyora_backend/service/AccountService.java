package org.example.flyora_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.flyora_backend.DTOs.AccountDTO;
import org.example.flyora_backend.DTOs.UserDTO;
import org.example.flyora_backend.model.*;
import org.example.flyora_backend.repository.*;
import org.example.flyora_backend.Utils.IdGeneratorUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final ShopOwnerRepository shopOwnerRepository;
    private final SalesStaffRepository salesStaffRepository;
    private final IdGeneratorUtil idGeneratorUtil;
    private final AccessLogRepository accessLogRepository;
    private final NotificationRepository notificationRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ChatBotRepository chatBotRepository;
    private final IssueReportRepository issueReportRepository;
    private final PromotionRepository promotionRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final InventoryRepository inventoryRepository;
    private final SystemLogRepository systemLogRepository;
    private final FaqRepository faqRepository;
    private final PolicyRepository policyRepository;

    @Transactional
    public Account createAccount(AccountDTO dto) {
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role"));

        // 1. Tạo tài khoản Account
        // 1. Tạo tài khoản Account
        Account acc = new Account();
        acc.setId(idGeneratorUtil.generateAccountId());
        acc.setUsername(dto.getUsername());
        acc.setPassword(dto.getPassword());
        acc.setPhone(dto.getPhone());
        acc.setEmail(dto.getEmail());
        acc.setRole(role);

        // ✅ Mặc định luôn active & approved
        acc.setIsActive(true);
        acc.setIsApproved(true);

        // Set approvedBy nếu có
        if (dto.getApprovedBy() != null) {
            Admin approver = adminRepository.findById(dto.getApprovedBy())
                    .orElseThrow(() -> new RuntimeException("Admin duyệt không tồn tại"));
            acc.setApprovedBy(approver);
        }

        accountRepository.save(acc);

        // 2. Tạo bản ghi cụ thể theo role
        switch (role.getName()) {
            case "Admin" -> {
                Admin admin = new Admin();
                admin.setId(idGeneratorUtil.generateAdminId());
                admin.setName(dto.getName());
                admin.setAccount(acc);
                adminRepository.save(admin);
            }
            case "Customer" -> {
                Customer customer = new Customer();
                customer.setId(idGeneratorUtil.generateCustomerId());
                customer.setName(dto.getName());
                customer.setEmail(acc.getEmail());
                customer.setAccount(acc);
                customerRepository.save(customer);
            }
            case "ShopOwner" -> {
                ShopOwner owner = new ShopOwner();
                owner.setId(idGeneratorUtil.generateShopOwnerId());
                owner.setName(dto.getName());
                owner.setAccount(acc);
                shopOwnerRepository.save(owner);
            }
            case "SalesStaff" -> {
                SalesStaff staff = new SalesStaff();
                staff.setId(idGeneratorUtil.generateSalesStaffId());
                staff.setName(dto.getName());
                staff.setAccount(acc);
                salesStaffRepository.save(staff);
            }
            default -> throw new RuntimeException("Role không hợp lệ để tạo người dùng cụ thể");
        }

        return acc;
    }

    @Transactional
    public List<UserDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public Account updateAccount(Integer id, AccountDTO dto) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        acc.setUsername(dto.getUsername());
        acc.setPassword(dto.getPassword());
        acc.setPhone(dto.getPhone());
        acc.setEmail(dto.getEmail());

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role"));
            acc.setRole(role);
        }

        if (dto.getApprovedBy() != null) {
            Admin admin = adminRepository.findById(dto.getApprovedBy())
                    .orElseThrow(() -> new RuntimeException("Admin duyệt không tồn tại"));
            acc.setApprovedBy(admin);
        }

        acc.setIsActive(dto.getIsActive());
        acc.setIsApproved(dto.getIsApproved());

        return accountRepository.save(acc);
    }

    @Transactional
    public void deleteAccount(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        String role = account.getRole().getName();

        // ✅ Xóa access log & notification liên quan
        accessLogRepository.deleteByAccountId(id);
        notificationRepository.deleteByRecipientId(id);

        // ✅ Nếu là CUSTOMER, xóa theo thứ tự phụ thuộc
        if ("Customer".equals(role)) {
            Customer customer = customerRepository.findByAccountId(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy customer liên kết với account"));

            Integer customerId = customer.getId();
            // 1. ProductReview → ChatBot → IssueReport → Promotion
            productReviewRepository.deleteByCustomerId(customerId);
            chatBotRepository.deleteByCustomerId(customerId);
            issueReportRepository.deleteByCustomerId(customerId);
            promotionRepository.deleteByCustomerId(customerId);

            // 2. Payment
            paymentRepository.deleteByCustomerId(customerId);

            // 3. OrderItem → DeliveryNote → Order
            List<Integer> orderIds = orderRepository.findIdsByCustomerId(customerId);
            for (Integer orderId : orderIds) {
                orderItemRepository.deleteByOrderId(orderId);
                deliveryNoteRepository.deleteByOrderId(orderId);
            }
            orderRepository.deleteByCustomerId(customerId);

            // 4. Xóa bản ghi Customer
            customerRepository.deleteByAccountId(id);
        }

        // ✅ Các vai trò khác: đơn giản hơn
        else if ("ShopOwner".equals(role)) {
            shopOwnerRepository.deleteByAccountId(id);
        } else if ("SalesStaff".equals(role)) {
            // Gọi phương thức mới, chính xác trong InventoryRepository
            inventoryRepository.deleteBySalesStaffAccountId(id);

            // Sau đó xóa bản ghi SalesStaff
            salesStaffRepository.deleteByAccountId(id);
        } else if ("Admin".equals(role)) {
            systemLogRepository.deleteByAdminAccountId(id);
            faqRepository.clearUpdatedBy(id);
            policyRepository.clearUpdatedBy(id);
            // cập nhật lại các approvedBy khác nếu cần
            adminRepository.deleteByAccountId(id);
        }

        // ✅ Cuối cùng xóa Account
        accountRepository.deleteById(id);
    }

    @Transactional
    public Account setActiveStatus(Integer id, boolean isActive) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));
        acc.setIsActive(isActive);
        return accountRepository.save(acc);
    }

}