package com.example.product_service.dto.response.variant;

import com.example.product_service.entity.ProductVariant;
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
        this.productPrice = new ProductPrice(productVariant);
        this.thumbnailUrl = productVariant.getProduct().getImages().stream().filter(i -> i.getSortOrder() == 0)
                .findFirst().get().getImageUrl();
        this.itemOptions = productVariant.getProductVariantOptions().stream().map(pvo ->
                new ItemOptionResponse(pvo.getOptionValue().getOptionValue(), pvo.getOptionValue().getOptionType().getName())).toList();
    }
}
