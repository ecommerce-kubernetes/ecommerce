package com.example.product_service.dto.response.product;

import com.example.product_service.entity.ProductVariants;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantsResponseDto {
    private Long id;
    private String sku;
    private int price;
    private int stockQuantity;
    private int discountValue;
    private List<Long> optionValueId;

    public VariantsResponseDto(ProductVariants productVariant){
        this.id = productVariant.getId();
        this.sku = productVariant.getSku();
        this.price = productVariant.getPrice();
        this.stockQuantity = productVariant.getStockQuantity();
        this.discountValue = productVariant.getDiscountValue();
        this.optionValueId = productVariant.getProductVariantOptions().stream()
                .map((pvo) -> pvo.getOptionValue().getId()).toList();
    }
}
