package com.example.order_service.dto.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private int price;
    private int stockQuantity;
    private Long categoryId;
}
