package com.example.product_service.dto.response.variant;

import com.example.product_service.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ProductPrice {
    private long unitPrice;
    private int discountRate;
    private long discountAmount;
    private long discountedPrice;

    public ProductPrice(ProductVariant productVariant){
        this.unitPrice = productVariant.getPrice();
        this.discountRate = productVariant.getDiscountValue();
        this.discountAmount = productVariant.getDiscountPrice();
        this.discountedPrice = productVariant.getPrice() - productVariant.getDiscountPrice();
    }
}
