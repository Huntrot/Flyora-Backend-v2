package org.example.flyora_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "Product")
public class Product {
    @Id
    private Integer id;

    private String description;

    private String name;

    private BigDecimal price;

    private Integer stock;

    @Builder.Default
    private Boolean status = true;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ProductCategory category;

    @ManyToOne
    @JoinColumn(name = "bird_type_id")
    private BirdType birdType;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private FoodDetail foodDetail;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private ToyDetail toyDetail;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL)
    private FurnitureDetail furnitureDetail;

    @ManyToOne
    @JoinColumn(name = "shop_owner_id")
    private ShopOwner shopOwner;

    @Column(nullable = false)
    @Builder.Default
    private Integer salesCount = 0;
}
