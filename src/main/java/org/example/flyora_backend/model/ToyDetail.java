package org.example.flyora_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "ToyDetail")
public class ToyDetail {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Size(max = 255)
    @Column(name = "material")
    private String material;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Size(max = 100)
    @Column(name = "color", length = 100)
    private String color;

    @Size(max = 255)
    @Column(name = "origin")
    private String origin;

    @Size(max = 255)
    @Column(name = "dimensions")
    private String dimensions;

    @Size(max = 255)
    @Column(name = "image_url")
    private String imageUrl;

}