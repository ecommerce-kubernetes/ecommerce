package com.example.product_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearch {
    @Min(value = 1, message = "{Min}")
    private Long categoryId;
    @Size(min = 1, message = "{NotBlank}")
    private String name;
    @Min(value = 0, message = "{Min}")
    @Max(value = 5, message = "{Max}")
    private Integer rating;
}
