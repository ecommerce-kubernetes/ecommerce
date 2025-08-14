package com.example.product_service.dto.response.product;

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
}
