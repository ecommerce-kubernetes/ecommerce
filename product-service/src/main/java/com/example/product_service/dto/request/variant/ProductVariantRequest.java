package com.example.product_service.dto.request.variant;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class ProductVariantRequest {
    @NotBlank(message = "{NotBlank}")
    private String sku;
    @NotNull(message = "{NotNull}")
    @Min(value = 0, message = "{Min}")
    @Max(value = 100000000, message = "{Max}")
    private Integer price;
    @NotNull(message = "{NotNull}")
    @Min(value = 1, message = "{Min}")
    private Integer stockQuantity;
    @Min(value = 0, message = "{Min}")
    @Max(value = 100, message = "{Max}")
    private Integer discountRate;
    private List<VariantOptionValueRequest> variantOption;
}
