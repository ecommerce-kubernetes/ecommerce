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
public class OptionTypeRequest {
    @NotBlank(message = "{optionType.name.notBlank}")
    private String name;
}
