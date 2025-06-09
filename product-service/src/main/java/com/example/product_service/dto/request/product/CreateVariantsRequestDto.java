package com.example.product_service.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateVariantsRequestDto {
    @NotEmpty(message = "Variants is not empty") @Valid
    private List<VariantsRequestDto> variants;
}
