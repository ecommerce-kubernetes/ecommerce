package com.example.product_service.dto.response.product;

import com.example.product_service.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateResponse {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;

    public ProductUpdateResponse(Products products){
        this.id = products.getId();
        this.name = products.getName();
        this.description = products.getDescription();
        this.categoryId = products.getCategory().getId();
    }
}
