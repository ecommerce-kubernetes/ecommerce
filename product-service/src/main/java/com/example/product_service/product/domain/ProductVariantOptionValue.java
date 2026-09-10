package com.example.product_service.product.domain;

import com.example.product_service.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariantOptionValue extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant productVariant;

    private Long optionValueId;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductVariantOptionValue(Long id, Long optionValueId, ProductVariant productVariant) {
        Assert.notNull(id, "상품 변형 옵션 아이디는 필수이다");
        Assert.notNull(productVariant, "상품 변형 옵션 상품 변형은 필수이다");
        Assert.notNull(optionValueId, "상품 변형 옵션 옵션 값 아이디는 필수이다");

        this.id = id;
        this.productVariant = productVariant;
        this.optionValueId = optionValueId;
    }

    public static ProductVariantOptionValue create(Long id, Long optionValueId, ProductVariant productVariant) {
        return ProductVariantOptionValue.builder()
                .id(id)
                .productVariant(productVariant)
                .optionValueId(optionValueId)
                .build();
    }
}
