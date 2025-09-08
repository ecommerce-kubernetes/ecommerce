package com.example.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String thumbNailUrl;
    private int quantity;
    private UnitPriceInfo unitPriceInfo;
    private int totalPrice;
    private List<ItemOptionResponse> options;
}
