package com.example.product_service.dto.response.product;

import com.example.product_service.dto.response.image.ImageResponse;
import com.example.product_service.dto.response.options.OptionValueResponse;
import com.example.product_service.dto.response.options.ProductOptionTypeResponse;
import com.example.product_service.dto.response.variant.ProductVariantResponse;
import com.example.product_service.entity.Products;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageResponse> images;
    private List<ProductOptionTypeResponse> productOptionTypes;
    private List<ProductVariantResponse> productVariants;


    public ProductResponse(Products products){
        this.id = products.getId();
        this.name = products.getName();
        this.description = products.getDescription();
        this.categoryId = products.getCategory().getId();
        this.createdAt = products.getCreateAt();
        this.updatedAt = products.getUpdateAt();
        this.images = products.getImages().stream()
                .map(i -> new ImageResponse(i.getId(), i.getImageUrl(), i.getSortOrder())).toList();
        this.productOptionTypes = products.getProductOptionTypes().stream()
                .map(pr -> new ProductOptionTypeResponse(pr.getOptionType().getId(), pr.getOptionType().getName())).toList();
    }
}
