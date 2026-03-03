package org.example.flyora_backend.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.flyora_backend.DTOs.*;
import org.example.flyora_backend.model.*;
import org.example.flyora_backend.repository.*;
import org.example.flyora_backend.Utils.IdGeneratorUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final DeliveryNoteRepository deliveryNoteRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final IdGeneratorUtil idGeneratorUtil;
    private final GHNService ghnService;
    private final EmailService emailService;

    @Override
    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode).orElse(null);
    }

    @Override
    public void save(Order order) {
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(CreateOrderDTO dto) {
        Order order = new Order();
        order.setId(idGeneratorUtil.generateOrderId());

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + dto.getCustomerId()));
        order.setCustomer(customer);
        order.setStatus("PENDING");
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        String orderCode = String.valueOf(System.currentTimeMillis());
        order.setOrderCode(orderCode);

        List<OrderItem> orderItems = new ArrayList<>();
        for (var itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemDTO.getProductId()));

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ hàng.");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setId(idGeneratorUtil.generateOrderItemId());
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
        }

        order.setOrderDetails(orderItems);

        Order savedOrder = orderRepository.save(order);

        return Map.of(
                "orderId", savedOrder.getId(),
                "orderCode", savedOrder.getOrderCode(),
                "status", savedOrder.getStatus());
    }
////////////////////////////////////////////////////////////
    @Override
    @Transactional
    public Map<String, Object> createPayment(CreatePaymentDTO dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(dto.getCustomerId())) {
            throw new RuntimeException("Customer ID không khớp với đơn hàng.");
        }

        if (dto.getPaymentMethodId() == 2) {
            try {
                CreateOrderRequestDTO ghnRequest = createGhnRequestFromOrder(order, dto);
                Map<String, Object> ghnResponse = ghnService.createOrder(ghnRequest);

                String trackingNumber = (String) ghnResponse.get("order_code");
                ShippingMethod shippingMethod = shippingMethodRepository.findById(1).orElse(null);

                DeliveryNote deliveryNote = new DeliveryNote();
                deliveryNote.setId(idGeneratorUtil.generateDeliveryNoteId());
                deliveryNote.setOrder(order);
                deliveryNote.setTrackingNumber(trackingNumber);
                deliveryNote.setShippingMethod(shippingMethod);
                deliveryNote.setDeliveryPartnerName("GHN");
                deliveryNote.setStatus("ready_to_pick");
                deliveryNoteRepository.save(deliveryNote);

                order.setStatus("Shipping");
                for (OrderItem item : order.getOrderDetails()) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() - item.getQuantity());
                    productRepository.save(product);
                }
                orderRepository.save(order);
                emailService.sendOrderConfirmationEmail(order);

            } catch (Exception e) {
                throw new RuntimeException("Tạo đơn vận chuyển thất bại: " + e.getMessage(), e);
            }
        }

        Payment payment = new Payment();
        payment.setId(idGeneratorUtil.generatePaymentId());
        payment.setOrder(order);
        payment.setCustomer(order.getCustomer());
        payment.setStatus("PENDING_COD");
        paymentRepository.save(payment);

        return Map.of("paymentId", payment.getId(), "orderStatus", order.getStatus());
    }
//////////////////////////////////////////////////////////

    @Transactional
    public void attachOrderCode(Integer orderId, String orderCode) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setOrderCode(orderCode); // cập nhật từ mã PayOS
        orderRepository.save(order);
    }

    private CreateOrderRequestDTO createGhnRequestFromOrder(Order order, CreatePaymentDTO paymentDTO) {
        CreateOrderRequestDTO ghnRequest = new CreateOrderRequestDTO();
        Customer customer = order.getCustomer();

        ghnRequest.setTo_name(customer.getName());
        ghnRequest.setTo_phone("0942921287");
        ghnRequest.setTo_address("123 Đường ABC");
        ghnRequest.setTo_ward_code("20309");
        ghnRequest.setTo_district_id(1459);

        BigDecimal codAmount = order.getOrderDetails().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ghnRequest.setCod_amount(codAmount.intValue());
        ghnRequest.setContent("Flyora - Don hang #" + order.getId());

        int totalWeight = order.getOrderDetails().stream()
                .mapToInt(item -> {
                    Product product = item.getProduct();
                    BigDecimal weight = BigDecimal.ZERO;

                    if (product.getFoodDetail() != null && product.getFoodDetail().getWeight() != null) {
                        weight = product.getFoodDetail().getWeight();
                    } else if (product.getToyDetail() != null && product.getToyDetail().getWeight() != null) {
                        weight = product.getToyDetail().getWeight();
                    } else if (product.getFurnitureDetail() != null
                            && product.getFurnitureDetail().getWeight() != null) {
                        weight = product.getFurnitureDetail().getWeight();
                    }

                    return weight.intValue() * item.getQuantity();
                }).sum();

        ghnRequest.setWeight(totalWeight > 0 ? totalWeight : 200);
        ghnRequest.setLength(20);
        ghnRequest.setWidth(20);
        ghnRequest.setHeight(10);
        ghnRequest.setInsurance_value(codAmount.intValue());
        ghnRequest.setService_id(53321);
        ghnRequest.setPayment_type_id(2);
        ghnRequest.setNote("Ghi chú từ đơn hàng");
        ghnRequest.setRequired_note("CHOXEMHANGKHONGTHU");

        List<ItemDTO> ghnItems = order.getOrderDetails().stream().map(item -> {
            ItemDTO ghnItem = new ItemDTO();
            ghnItem.setName(item.getProduct().getName());
            ghnItem.setQuantity(item.getQuantity());
            ghnItem.setPrice(item.getPrice().intValue());
            return ghnItem;
        }).toList();
        ghnRequest.setItems(ghnItems);

        return ghnRequest;
    }

    @Override
    public List<OrderHistoryDTO> getOrdersByCustomer(Integer customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        return orders.stream().map(order -> {
            List<OrderDetailDTO> details = order.getOrderDetails().stream().map(detail -> new OrderDetailDTO(
                    detail.getProduct().getId(),
                    detail.getProduct().getName(),
                    detail.getQuantity(),
                    detail.getPrice())).toList();

            return new OrderHistoryDTO(
                    order.getId(),
                    order.getCreatedAt(),
                    order.getStatus(),
                    order.getOrderCode(),
                    details);
        }).toList();
    }
}