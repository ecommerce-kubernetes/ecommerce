package com.example.product_service.dto.response.product;

import com.example.product_service.entity.ProductOptionTypes;
import com.example.product_service.entity.ProductVariants;
import com.example.product_service.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private List<ProductImageDto> images;

    private List<Long> optionTypes;
    private List<VariantsResponseDto> variants;

    public ProductResponseDto(Products product){
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory().getId();
        this.images = product.getImages().stream().map(ProductImageDto::new).toList();
        this.optionTypes = product.getProductOptionTypes().stream().map(ProductOptionTypes::getId).toList();
        this.variants = product.getProductVariants().stream().map((VariantsResponseDto::new)).toList();
    }
}
