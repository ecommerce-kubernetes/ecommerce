package com.example.product_service.dto.response;

import com.example.product_service.entity.ProductImages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {
    private Long id;
    private String imageUrl;
    private int sortOrder;

    public ProductImageDto(ProductImages prImg){
        this.id = prImg.getId();
        this.imageUrl = prImg.getImageUrl();
        this.sortOrder = prImg.getSortOrder();
    }
}
