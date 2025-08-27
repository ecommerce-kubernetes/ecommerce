package com.example.product_service.dto.response.product;

import com.example.product_service.entity.Product;
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

    public ProductUpdateResponse(Product product){
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.categoryId = product.getCategory().getId();
    }
}
