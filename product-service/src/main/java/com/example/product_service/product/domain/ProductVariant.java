package com.example.product_service.product.domain;

import com.example.product_service.common.exception.BusinessException;
import com.example.product_service.product.domain.context.AddVariantContext;
import com.example.product_service.product.domain.vo.SalePrice;
import com.example.product_service.product.exception.ProductErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariant {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private ProductVariantStatus status;

    private String sku;

    @Embedded
    private SalePrice salePrice;

    private Integer stock;

    private LocalDateTime discontinuedAt;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productVariant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariantOptionValue> productVariantOptionValues = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private ProductVariant(Long id, Product product, ProductVariantStatus status, String sku, SalePrice salePrice, int stock,
                           LocalDateTime discontinuedAt) {
        Assert.notNull(id, "상품 변형 아이디는 필수이다");
        Assert.notNull(product, "상품 변형 상품은 필수이다");
        Assert.notNull(status, "상품 변형 상태는 필수이다");
        Assert.notNull(sku, "상품 변형 SKU는 필수이다");
        Assert.notNull(salePrice, "상품 변형 판매 가격은 필수이다");

        this.id = id;
        this.product = product;
        this.status = status;
        this.sku = sku;
        this.salePrice = salePrice;
        this.stock = stock;
        this.discontinuedAt = discontinuedAt;
    }

    public static ProductVariant create(AddVariantContext context, Product product) {
        if (context.stock() < 0) {
            throw new BusinessException(ProductErrorCode.VARIANT_INVALID_STOCK);
        }

        ProductVariant variant = ProductVariant.builder()
                .id(context.id())
                .product(product)
                .status(ProductVariantStatus.PREPARING)
                .sku(context.sku())
                .salePrice(context.salePrice())
                .stock(context.stock())
                .build();

        for (AddVariantContext.AddVariantOptionValueContext optionValue : context.optionValues()) {
            ProductVariantOptionValue productVariantOptionValue = ProductVariantOptionValue.create(optionValue.id(), optionValue.optionValueId(), variant);
            variant.addProductVariantOptionValue(productVariantOptionValue);
        }
        return variant;
    }

    private void addProductVariantOptionValue(ProductVariantOptionValue productVariantOptionValue) {
        this.productVariantOptionValues.add(productVariantOptionValue);
    }

    public void discontinued(LocalDateTime discontinuedAt) {
        this.status = ProductVariantStatus.DISCONTINUED;
        this.discontinuedAt = discontinuedAt;
    }

}
