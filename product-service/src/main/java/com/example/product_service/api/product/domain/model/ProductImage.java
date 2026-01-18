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
public class ProductImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String imageUrl;
    private Integer sortOrder;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductImage(String imageUrl, int sortOrder){
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public static ProductImage create(Product product, String imageUrl, int sortOrder) {
        ProductImage image = ProductImage.builder()
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
        image.setProduct(product);
        return image;
    }

    public boolean isThumbnail() {
        return this.sortOrder == 1;
    }

    protected void setProduct(Product product) {
        this.product = product;
    }
}
