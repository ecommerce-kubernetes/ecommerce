package com.example.product_service.dto.response.product;

import com.example.product_service.entity.ProductImages;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
