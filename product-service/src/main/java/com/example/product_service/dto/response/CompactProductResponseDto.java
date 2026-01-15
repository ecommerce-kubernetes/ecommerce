package com.example.product_service.dto.response;

import com.example.product_service.api.product.domain.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompactProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private int price;
    private int stockQuantity;
    private Long categoryId;
    private String mainImgUrl;

    public CompactProductResponseDto(Product product, String mainImgUrl){
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory().getId();
        this.mainImgUrl = mainImgUrl;
    }
}
