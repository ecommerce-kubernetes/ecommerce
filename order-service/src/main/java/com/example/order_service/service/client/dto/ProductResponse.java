package com.example.order_service.service.client.dto;

import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private UnitPrice unitPrice;
    private String thumbnailUrl;
    private List<ItemOptionResponse> itemOptions;

    //TODO private 접근자로 변경
    @Builder
    public ProductResponse(Long productId, Long productVariantId, String productName, UnitPrice unitPrice, String thumbnailUrl,
                           List<ItemOptionResponse> itemOptions){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.thumbnailUrl = thumbnailUrl;
        this.itemOptions = itemOptions;
    }
}
