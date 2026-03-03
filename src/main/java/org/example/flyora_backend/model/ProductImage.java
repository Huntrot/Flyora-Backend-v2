package org.example.flyora_backend.model;



import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ProductImage")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String imageUrl;

    @Builder.Default
    private Boolean status = true;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
