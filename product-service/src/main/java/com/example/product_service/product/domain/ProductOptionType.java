package com.example.product_service.product.domain;

import com.example.product_service.common.entity.BaseEntity;
import com.example.product_service.option.domain.OptionType;
import com.example.product_service.product.domain.context.RegisterOptionTypeContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionType extends BaseEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Long optionTypeId;

    private Integer displayOrder;

    @Builder(access = AccessLevel.PRIVATE)
    private ProductOptionType(Long id, Product product, Long optionTypeId, int displayOrder) {
        Assert.notNull(id, "상품 옵션 아이디는 필수이다.");
        Assert.notNull(product, "상품 옵션 상품은 필수이다.");
        Assert.notNull(optionTypeId, "상품 옵션 옵션 타입 아이디는 필수이다.");
        Assert.notNull(displayOrder, "상품 옵션 우선순위는 필수이다.");

        this.id = id;
        this.product = product;
        this.optionTypeId = optionTypeId;
        this.displayOrder = displayOrder;
    }

    public static ProductOptionType create(RegisterOptionTypeContext context, Product product, int displayOrder) {
        return ProductOptionType.builder()
                .id(context.id())
                .product(product)
                .optionTypeId(context.optionTypeId())
                .displayOrder(displayOrder)
                .build();
    }
}
