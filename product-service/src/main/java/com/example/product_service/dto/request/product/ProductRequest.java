package com.example.product_service.dto.request.product;

import com.example.product_service.dto.request.image.ImageRequest;
import com.example.product_service.dto.request.options.ProductOptionTypeRequest;
import com.example.product_service.dto.request.variant.ProductVariantRequest;
import jakarta.validation.Valid;
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
    @NotBlank(message = "{NotBlank}")
    private String name;
    private String description;
    @NotNull(message = "{NotNull}")
    private Long categoryId;
    @NotEmpty(message = "{NotEmpty}") @Valid
    private List<ImageRequest> images;
    @Valid
    private List<ProductOptionTypeRequest> productOptionTypes;
    @NotEmpty(message = "{NotEmpty}") @Valid
    private List<ProductVariantRequest> productVariants;
}
