package com.example.product_service.service.dto;

import com.example.product_service.entity.ProductVariant;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InventoryReductionItem {
    private Long productVariantId;
    private int price;
    private int discountPrice;

    public InventoryReductionItem(ProductVariant productVariant){
        this.productVariantId = productVariant.getId();
        this.price = productVariant.getPrice();
        this.discountPrice = productVariant.getDiscountPrice();
    }
}
