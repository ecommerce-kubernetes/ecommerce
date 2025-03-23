package com.example.product_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {
    private String name;
    private String description;
    private int price;
    private int stockQuantity;
    private Long categoryId;
}
