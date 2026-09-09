package com.example.product_service.product.domain;

import com.example.product_service.common.entity.BaseEntity;
import com.example.product_service.product.domain.context.AddMainImageContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductMainImage extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String imagePath;

    private Integer sortOrder;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductMainImage(Long id, Product product, String imagePath, int sortOrder){
        Assert.notNull(id, "상품 메인 이미지 아이디는 필수이다");
        Assert.notNull(product, "상품 메인 이미지 상품은 필수이다");
        Assert.notNull(imagePath, "상품 메인 이미지 이미지 경로는 필수이다");

        this.id = id;
        this.product = product;
        this.imagePath = imagePath;
        this.sortOrder = sortOrder;
    }

    public static ProductMainImage create(AddMainImageContext context, Product product, int sortOrder) {
        return ProductMainImage.builder()
                .id(context.id())
                .product(product)
                .imagePath(context.imagePath())
                .sortOrder(sortOrder)
                .build();
    }

    public boolean isThumbnail() {
        return this.sortOrder == 1;
    }
}
