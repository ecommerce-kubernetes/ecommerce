package com.example.product_service.dto.request.product;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    private Long categoryId;
    private List<ImageRequest> images;
    private List<ProductOptionTypeRequest> productOptionTypes;
    private List<ProductVariantRequest> productVariants;
}
