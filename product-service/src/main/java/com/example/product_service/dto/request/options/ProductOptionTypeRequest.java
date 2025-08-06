package com.example.product_service.dto.request.options;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionTypeRequest {
    @NotNull(message = "{NotNull}")
    private Long optionTypeId;
    @NotNull(message = "{NotNull}")
    @Min(value = 0, message = "{Min}")
    private Integer priority;
}
