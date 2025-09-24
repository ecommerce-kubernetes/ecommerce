package com.example.product_service.dto.request.variant;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VariantOptionValueRequest {
    @NotNull(message = "{NotNull}")
    private Long optionTypeId;
    @NotNull(message = "{NotNull}")
    private Long optionValueId;
}
