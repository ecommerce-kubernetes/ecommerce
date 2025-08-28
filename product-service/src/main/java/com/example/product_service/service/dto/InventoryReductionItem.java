package com.example.product_service.service.dto;

import com.example.product_service.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InventoryReductionItem {
    private Long productVariantId;
    private int price;
    private int stock;
    private int discountPrice;

    public InventoryReductionItem(ProductVariant productVariant, int stock){
        this.productVariantId = productVariant.getId();
        this.price = productVariant.getPrice();
        this.stock = stock;
        this.discountPrice = productVariant.getDiscountPrice();
    }
}
