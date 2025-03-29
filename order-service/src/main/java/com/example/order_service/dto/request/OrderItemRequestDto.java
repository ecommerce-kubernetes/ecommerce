package com.example.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDto {
    private Long productId;
    private String productName;
    private int quantity;
}
