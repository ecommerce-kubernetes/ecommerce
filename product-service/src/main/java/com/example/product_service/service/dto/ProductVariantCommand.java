package com.example.product_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductVariantCommand {
    private String sku;
    private int price;
    private int stockQuantity;
    private int discountRate;
    private List<VariantOptionValueRef> variantOptionValues;
}
