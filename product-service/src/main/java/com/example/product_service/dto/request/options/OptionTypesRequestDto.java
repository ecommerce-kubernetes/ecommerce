package com.example.product_service.dto.request.options;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptionTypesRequestDto {
    @NotBlank(message = "name is required")
    private String name;
}
