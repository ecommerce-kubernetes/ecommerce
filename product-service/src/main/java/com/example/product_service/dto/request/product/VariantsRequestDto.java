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
    @NotNull(message = "price is required") @Min(0) @Max(50000000)
    private int price;
    @NotNull(message = "stockQuantity is required") @Min(0) @Max(1000)
    private int stockQuantity;
    private int discountValue;

    private List<Long> optionValueIds;
}
