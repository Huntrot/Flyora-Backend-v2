package org.example.flyora_backend.service;

import java.util.Map;

import org.example.flyora_backend.DTOs.LoginDTO;
import org.example.flyora_backend.DTOs.LoginResponseDTO;
import org.example.flyora_backend.DTOs.RegisterDTO;
import org.example.flyora_backend.model.Account;
import org.example.flyora_backend.model.Admin;
import org.example.flyora_backend.model.Customer;
import org.example.flyora_backend.model.Role;
import org.example.flyora_backend.model.SalesStaff;
import org.example.flyora_backend.model.ShopOwner;
import org.example.flyora_backend.repository.AccountRepository;
import org.example.flyora_backend.repository.AdminRepository;
import org.example.flyora_backend.repository.CustomerRepository;
import org.example.flyora_backend.repository.RoleRepository;
import org.example.flyora_backend.repository.SalesStaffRepository;
import org.example.flyora_backend.repository.ShopOwnerRepository;
import org.example.flyora_backend.Utils.IdGeneratorUtil;
import org.example.flyora_backend.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final ShopOwnerRepository shopOwnerRepository;
    private final AdminRepository adminRepository;
    private final SalesStaffRepository salesStaffRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    private AccessLogService accessLogService;

    @Autowired
    private IdGeneratorUtil idGeneratorUtil;

    @Autowired
    private EmailService emailService;

    @Override
    public Map<String, Object> registerCustomer(RegisterDTO request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        Role customerRole = roleRepository.findByName("Customer")
                .orElseThrow(() -> new RuntimeException("Role CUSTOMER không tồn tại"));

        Account account = new Account();
        account.setId(idGeneratorUtil.generateAccountId()); // 👈 Gán ID thủ công
        account.setUsername(request.getUsername());
        account.setPassword(request.getPassword());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setRole(customerRole);
        account.setIsActive(true);
        account.setIsApproved(true);
        accountRepository.save(account);

        Customer customer = new Customer();
        customer.setId(idGeneratorUtil.generateCustomerId()); // 👈 Gán ID thủ công
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setAccount(account);
        customerRepository.save(customer);

        return Map.of("message", "Đăng ký thành công", "userId", customer.getId());
    }




    @Override
    public LoginResponseDTO loginCustomer(LoginDTO request) {
        Account account = accountRepository
                .findByUsernameAndPassword(request.getUsername(), request.getPassword())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sai tên đăng nhập hoặc mật khẩu"));

        if (!account.getIsActive() || !account.getIsApproved()) {
            throw new RuntimeException("Tài khoản chưa được kích hoạt");
        }

        String roleName = account.getRole().getName();

        LoginResponseDTO response = new LoginResponseDTO();
        response.setUserId(account.getId());
        response.setName(account.getUsername());
        response.setRole(roleName);

        switch (roleName) {
            case "Customer" -> {
                Customer c = customerRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy customer"));
                response.setName(c.getName());
                response.setLinkedId(c.getId());
            }
            case "ShopOwner" -> {
                ShopOwner s = shopOwnerRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy shop owner"));
                response.setName(s.getName());
                response.setLinkedId(s.getId());
            }
            case "Admin" -> {
                Admin a = adminRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));
                response.setName(a.getName());
                response.setLinkedId(a.getId());
            }
            case "SalesStaff" -> {
                SalesStaff staff = salesStaffRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy staff"));
                response.setName(staff.getName());
                response.setLinkedId(staff.getId());
            }
        }

        String otp = emailService.createAndStoreOtp(account.getUsername());
        emailService.sendOTPEmail(account.getEmail(), otp);

        String preAuthToken = jwtUtil.generatePreAuthToken(account);
        response.setToken(preAuthToken);
        accessLogService.logAction(account.getId(), "Đăng nhập thành công bước 1. Tiến hành xác thực OTP.");
        return response;
    } 
}
