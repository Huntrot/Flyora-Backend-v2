package org.example.flyora_backend.Utils;

import jakarta.annotation.PostConstruct;
import org.example.flyora_backend.model.*; // Import các model cần thiết
import org.example.flyora_backend.repository.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IdGeneratorUtil {

    // Sử dụng một Map để lưu trữ bộ đếm ID cho tất cả các thực thể
    // AtomicInteger để đảm bảo an toàn trong môi trường đa luồng
    private final Map<String, AtomicInteger> idCounters = new ConcurrentHashMap<>();

    // Sử dụng constructor injection, đây là cách làm được khuyến khích
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final ProductReviewRepository productReviewRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final FoodDetailRepository foodDetailRepository;
    private final ToyDetailRepository toyDetailRepository;
    private final FurnitureDetailRepository furnitureDetailRepository;
    private final AdminRepository adminRepository;
    private final ShopOwnerRepository shopOwnerRepository;
    private final SalesStaffRepository salesStaffRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final SystemLogRepository systemLogRepository;

    public IdGeneratorUtil(AccountRepository accountRepository, CustomerRepository customerRepository,
            ProductReviewRepository productReviewRepository, OrderRepository orderRepository,
            OrderItemRepository orderItemRepository, PaymentRepository paymentRepository,
            ProductRepository productRepository, FoodDetailRepository foodDetailRepository,
            ToyDetailRepository toyDetailRepository, FurnitureDetailRepository furnitureDetailRepository,
            AdminRepository adminRepository, ShopOwnerRepository shopOwnerRepository,
            SalesStaffRepository salesStaffRepository, DeliveryNoteRepository deliveryNoteRepository,
            SystemLogRepository systemLogRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.productReviewRepository = productReviewRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.foodDetailRepository = foodDetailRepository;
        this.toyDetailRepository = toyDetailRepository;
        this.furnitureDetailRepository = furnitureDetailRepository;
        this.adminRepository = adminRepository;
        this.shopOwnerRepository = shopOwnerRepository;
        this.salesStaffRepository = salesStaffRepository;
        this.deliveryNoteRepository = deliveryNoteRepository;
        this.systemLogRepository = systemLogRepository;
    }

    /**
     * Phương thức này sẽ tự động được gọi một lần sau khi IdGeneratorUtil được tạo.
     * Nó sẽ khởi tạo tất cả các bộ đếm ID bằng cách lấy giá trị lớn nhất từ DB.
     */
    @PostConstruct
    public void init() {
        // Khởi tạo bộ đếm cho từng loại thực thể
        idCounters.put(Account.class.getSimpleName(), new AtomicInteger(accountRepository.findMaxId().orElse(0)));
        idCounters.put(Customer.class.getSimpleName(), new AtomicInteger(customerRepository.findMaxId().orElse(0)));
        idCounters.put(ProductReview.class.getSimpleName(),
                new AtomicInteger(productReviewRepository.findMaxId().orElse(0)));
        idCounters.put(Order.class.getSimpleName(), new AtomicInteger(orderRepository.findMaxId().orElse(0)));
        idCounters.put(OrderItem.class.getSimpleName(), new AtomicInteger(orderItemRepository.findMaxId().orElse(0)));
        idCounters.put(Payment.class.getSimpleName(), new AtomicInteger(paymentRepository.findMaxId().orElse(0)));
        idCounters.put(Product.class.getSimpleName(), new AtomicInteger(productRepository.findMaxId().orElse(0)));
        idCounters.put(FoodDetail.class.getSimpleName(), new AtomicInteger(foodDetailRepository.findMaxId().orElse(0)));
        idCounters.put(ToyDetail.class.getSimpleName(), new AtomicInteger(toyDetailRepository.findMaxId().orElse(0)));
        idCounters.put(FurnitureDetail.class.getSimpleName(),
                new AtomicInteger(furnitureDetailRepository.findMaxId().orElse(0)));
        idCounters.put(Admin.class.getSimpleName(), new AtomicInteger(adminRepository.findMaxId().orElse(0)));
        idCounters.put(ShopOwner.class.getSimpleName(), new AtomicInteger(shopOwnerRepository.findMaxId().orElse(0)));
        idCounters.put(SalesStaff.class.getSimpleName(), new AtomicInteger(salesStaffRepository.findMaxId().orElse(0)));
        idCounters.put(DeliveryNote.class.getSimpleName(),
                new AtomicInteger(deliveryNoteRepository.findMaxId().orElse(0)));
        idCounters.put(SystemLog.class.getSimpleName(), new AtomicInteger(systemLogRepository.findMaxId().orElse(0)));
    }

    // Phương thức chung để lấy ID mới
    private Integer getNextId(String entityName) {
        return idCounters.get(entityName).incrementAndGet();
    }

    // Các hàm generate cụ thể giờ chỉ cần gọi hàm chung
    public Integer generateProductReviewId() {
        return getNextId(ProductReview.class.getSimpleName());
    }

    public Integer generateAccountId() {
        return getNextId(Account.class.getSimpleName());
    }

    public Integer generateCustomerId() {
        return getNextId(Customer.class.getSimpleName());
    }

    public Integer generateOrderId() {
        return getNextId(Order.class.getSimpleName());
    }

    public Integer generateOrderItemId() {
        return getNextId(OrderItem.class.getSimpleName());
    }

    public Integer generatePaymentId() {
        return getNextId(Payment.class.getSimpleName());
    }

    public Integer generateToyDetailId() {
        return getNextId(ToyDetail.class.getSimpleName());
    }

    public Integer generateFoodDetailId() {
        return getNextId(FoodDetail.class.getSimpleName());
    }

    public Integer generateFurnitureDetailId() {
        return getNextId(FurnitureDetail.class.getSimpleName());
    }

    public Integer generateProductId() {
        return getNextId(Product.class.getSimpleName());
    }

    public Integer generateAdminId() {
        return getNextId(Admin.class.getSimpleName());
    }

    public Integer generateShopOwnerId() {
        return getNextId(ShopOwner.class.getSimpleName());
    }

    public Integer generateSalesStaffId() {
        return getNextId(SalesStaff.class.getSimpleName());
    }

    public Integer generateDeliveryNoteId() {
        return getNextId(DeliveryNote.class.getSimpleName());
    }

    public Integer generateSystemLogId() {
        return getNextId(SystemLog.class.getSimpleName());
    }
}