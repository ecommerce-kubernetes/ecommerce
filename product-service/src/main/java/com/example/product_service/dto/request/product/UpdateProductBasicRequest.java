package com.example.product_service.dto.request.product;

import com.example.product_service.dto.validation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@AtLeastOneFieldNotNull(message = "{EmptyRequest}")
public class UpdateProductBasicRequest {
    @Pattern(regexp = "^(?!\\s*$).+", message = "{NotBlank}")
    private String name;
    private String description;
    private Long categoryId;
}
