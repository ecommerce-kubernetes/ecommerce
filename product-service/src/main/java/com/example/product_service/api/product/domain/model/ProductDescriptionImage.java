package com.example.product_service.api.product.domain.model;

import com.example.product_service.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductDescriptionImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    private String imagePath;
    private Integer sortOrder;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductDescriptionImage(String imagePath, int sortOrder) {
        this.imagePath = imagePath;
        this.sortOrder = sortOrder;
    }

    public static ProductDescriptionImage create(Product product, String imagePath, int sortOrder) {
        ProductDescriptionImage image = ProductDescriptionImage.builder()
                .imagePath(imagePath)
                .sortOrder(sortOrder)
                .build();

        image.setProduct(product);
        return image;
    }

    protected void setProduct(Product product) {
        this.product = product;
    }
}
