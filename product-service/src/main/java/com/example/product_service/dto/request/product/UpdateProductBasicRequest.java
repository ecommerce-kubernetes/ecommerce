package com.example.product_service.dto.request.product;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UpdateProductBasicRequest {
    @Size(min = 1, message = "{NotBlank}")
    private String name;
    private String description;
    private Long categoryId;
}
