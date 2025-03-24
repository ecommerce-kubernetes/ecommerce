package com.example.product_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StockQuantityRequestDto {
    @Min(0) @Max(100)
    private int updateStockQuantity;
}
