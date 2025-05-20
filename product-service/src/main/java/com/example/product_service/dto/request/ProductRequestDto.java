package com.example.product_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
    @Min(value = 0, message = "Product price must not be less than 0") @Max(value = 10000000, message = "Product price must not be greater than 10,000,000")
    private int price;
    @Min(value = 0, message = "Product stockQuantity must not be less than 0") @Max(value = 100, message = "Product stockQuantity must not be greater than 100")
    private int stockQuantity;
    @NotNull(message = "Product categoryId is required")
    private Long categoryId;

    @NotEmpty(message = "At least one image URL is required")
    private List<@NotBlank @URL(message = "Invalid image URL") String> imageUrls;

}
