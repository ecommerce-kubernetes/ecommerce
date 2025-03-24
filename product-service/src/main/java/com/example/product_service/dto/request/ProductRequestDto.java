package com.example.product_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {
    @NotBlank(message = "Product name is required")
    private String name;
    @NotBlank(message = "Product description is required")
    private String description;
    @Min(value = 0, message = "Product price must not be less than 0") @Max(value = 1000000, message = "Product price must not be greater than 10,000,000")
    private int price;
    @Min(value = 0, message = "Product stockQuantity must not be less than 0") @Max(value = 100, message = "Product stockQuantity must not be greater than 100")
    private int stockQuantity;
    @NotNull(message = "Product categoryId is required")
    private Long categoryId;
}
