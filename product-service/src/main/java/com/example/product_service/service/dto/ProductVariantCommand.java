package com.example.product_service.service.dto;

import com.example.product_service.dto.request.variant.ProductVariantRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProductVariantCommand {
    private String sku;
    private int price;
    private int stockQuantity;
    private int discountRate;
    private List<VariantOptionValueRef> variantOptionValues;

    public ProductVariantCommand(ProductVariantRequest request){
        this.sku = request.getSku();
        this.price = request.getPrice();
        this.stockQuantity = request.getStockQuantity();
        this.discountRate = request.getDiscountRate();
        this.variantOptionValues = request.getVariantOption().stream().map(VariantOptionValueRef::new).toList();
    }
}
