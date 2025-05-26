package com.example.product_service.dto.request.options;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OptionValuesUpdateRequestDto {
    @NotBlank
    private String optionValue;
}
