package com.example.order_service.service.client.dto;

import com.example.order_service.dto.response.ItemOptionResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ProductResponse {
    private Long productId;
    private Long productVariantId;
    private String productName;
    private ProductPrice productPrice;
    private String thumbnailUrl;
    private List<ItemOptionResponse> itemOptions;
}
