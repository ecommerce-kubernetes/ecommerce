package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProductInfo {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private long price;
    private int discountRate;
    private String thumbnailUrl;
    private List<ItemOptionResponse> itemOptions;

    public long calcDiscountPrice(){
        return Math.round(price * (1 - discountRate / 100.0));
    }

    @Builder
    private ProductInfo(Long productId, long productVariantId, String productName, long price, int discountRate,
                        String thumbnailUrl, List<ItemOptionResponse> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.price = price;
        this.discountRate = discountRate;
        this.thumbnailUrl = thumbnailUrl;
        this.itemOptions = itemOptions;
    }
}
