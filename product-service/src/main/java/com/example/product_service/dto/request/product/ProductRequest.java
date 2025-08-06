package com.example.product_service.dto.request.product;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "{product.name.notBlank}")
    private String name;
    private String description;
    @NotNull(message = "{product.categoryId.notNull}")
    private Long categoryId;
    @NotEmpty(message = "{product.images.notEmpty}")
    private List<ImageRequest> images;
    private List<ProductOptionTypeRequest> productOptionTypes;
    @NotEmpty(message = "{product.productVariants.notEmpty}")
    private List<ProductVariantRequest> productVariants;
}
