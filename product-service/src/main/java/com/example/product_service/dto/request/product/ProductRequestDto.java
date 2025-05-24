package com.example.product_service.dto.request.product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {
    @NotBlank(message = "Product name is required")
    private String name;
    @NotBlank(message = "Product description is required")
    private String description;
    @NotNull(message = "Product categoryId is required")
    private Long categoryId;
    @NotEmpty(message = "At least one image URL is required")
    private List<@NotBlank @URL(message = "Invalid image URL") String> imageUrls;

    private List<Long> optionTypeIds;

    @NotEmpty(message = "Variants is not empty") @Valid
    private List<VariantsRequestDto> variants;
}
