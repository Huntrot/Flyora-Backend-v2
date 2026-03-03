package org.example.flyora_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "DeliveryNote")
public class DeliveryNote {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_method_id")
    private ShippingMethod shippingMethod;

    @Size(max = 255)
    @Column(name = "delivery_partner_name")
    private String deliveryPartnerName;

    @Size(max = 255)
    @Column(name = "tracking_number")
    private String trackingNumber;

    @Size(max = 100)
    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "estimated_delivery_date")
    private Instant estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private Instant actualDeliveryDate;
}
