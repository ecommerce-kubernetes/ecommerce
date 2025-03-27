package com.example.product_service.dto.response;

import com.example.product_service.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private int price;
    private int stockQuantity;
    private Long categoryId;

    public ProductResponseDto(Products product){
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.categoryId = product.getCategory().getId();
    }
}
