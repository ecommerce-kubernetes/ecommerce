package com.example.product_service.service.dto;

import com.example.product_service.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class InventoryReductionItem {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private String thumbnail;
    private int price;
    private int discountRate;
    private int discountAmount;
    private int finalPrice;
    private int stock;

    public InventoryReductionItem(ProductVariant productVariant, int stock){
        this.productId = productVariant.getProduct().getId();
        this.productVariantId = productVariant.getId();
        this.productName = productVariant.getProduct().getName();
        this.thumbnail = productVariant.getProduct().getImages().stream()
                .filter(p -> p.getSortOrder() == 0).findFirst().get().getImageUrl();
        this.price = productVariant.getPrice();
        this.discountRate = productVariant.getDiscountValue();
        this.discountAmount = productVariant.getDiscountPrice();
        this.finalPrice = productVariant.getDiscountPrice();
        this.stock = stock;
    }
}
