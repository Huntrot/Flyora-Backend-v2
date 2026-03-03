package org.example.flyora_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "`Order`") // tránh lỗi SQL từ khóa
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    private Integer id;
    @Column(name = "code")
    private String orderCode;
    
    @Column(name = "created_at")
    private Timestamp createdAt;

    private String status; // 🟢 Khớp với VARCHAR(100)

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderDetails;

    
}