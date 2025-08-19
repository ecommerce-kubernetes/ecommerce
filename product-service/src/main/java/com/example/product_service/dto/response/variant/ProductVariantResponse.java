package com.example.product_service.dto.response.variant;

import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.entity.ProductVariants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String sku;
    private int price;
    private int stockQuantity;
    private int discountRate;
    private List<OptionValueResponse> optionValues;

    public ProductVariantResponse(ProductVariants productVariants){
        this.id = productVariants.getId();
        this.sku = productVariants.getSku();
        this.price = productVariants.getPrice();
        this.stockQuantity = productVariants.getStockQuantity();
        this.discountRate = productVariants.getDiscountValue();
        this.optionValues = productVariants.getProductVariantOptions().stream()
                .map(ov -> new OptionValueResponse(ov.getOptionValue())).toList();
    }
}
