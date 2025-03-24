package com.example.product_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Min(0) @Max(1000000)
    private int price;
    @Min(0) @Max(100)
    private int stockQuantity;
    @NotNull
    private Long categoryId;
}
