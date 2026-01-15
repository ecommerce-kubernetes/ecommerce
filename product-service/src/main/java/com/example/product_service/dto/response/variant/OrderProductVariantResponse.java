package com.example.product_service.dto.response.variant;

import com.example.product_service.api.product.domain.model.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderProductVariantResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private ProductPrice productPrice;
    private String thumbnailUrl;
    private List<ItemOptionResponse> itemOptions;

    public OrderProductVariantResponse(ProductVariant productVariant){
        this.productId = productVariant.getProduct().getId();
        this.productVariantId = productVariant.getId();
        this.productName = productVariant.getProduct().getName();
        this.itemOptions = productVariant.getProductVariantOptions().stream().map(pvo ->
                new ItemOptionResponse(pvo.getOptionValue().getName(), pvo.getOptionValue().getOptionType().getName())).toList();
    }
}
