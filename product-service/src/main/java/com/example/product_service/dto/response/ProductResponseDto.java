package com.example.product_service.dto.response;

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
