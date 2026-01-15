package com.example.product_service.dto.response.variant;

import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.api.product.domain.model.ProductVariant;
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

    public ProductVariantResponse(ProductVariant productVariant){
        this.id = productVariant.getId();
        this.sku = productVariant.getSku();
        this.stockQuantity = productVariant.getStockQuantity();
        this.discountRate = productVariant.getDiscountRate();
        this.optionValues = productVariant.getProductVariantOptions().stream()
                .map(ov -> new OptionValueResponse(ov.getOptionValue())).toList();
    }
}
