package com.example.product_service.dto.request.options;

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
public class OptionValuesRequestDto {
    @NotNull(message = "optionTypeId is required")
    private Long optionTypeId;
    @NotBlank(message = "optionValue is required")
    private String optionValue;
}
