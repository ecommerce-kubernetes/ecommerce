package com.example.product_service.dto.request.product;

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
public class VariantsRequestDto {
    @NotBlank(message = "sku is required")
    private String sku;
    @NotNull(message = "price is required")
    @Min(value = 1, message = "price must be greater than 0")
    @Max(value = 50000000, message = "price must be less than 50000000")
    private int price;
    @NotNull(message = "stockQuantity is required")
    @Min(value = 1, message = "stockQuantity must be greater than 0")
    @Max(value = 1000, message = "stockQuantity must be less than 1000")
    private int stockQuantity;
    private int discountValue;

    private List<Long> optionValueIds;
}
