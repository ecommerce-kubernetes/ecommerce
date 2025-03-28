package com.example.product_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StockQuantityRequestDto {
    @Min(value = 0, message = "Product stockQuantity must not be less than 0") @Max(value = 100, message = "Product stockQuantity must not be greater than 100")
    private int updateStockQuantity;
}
