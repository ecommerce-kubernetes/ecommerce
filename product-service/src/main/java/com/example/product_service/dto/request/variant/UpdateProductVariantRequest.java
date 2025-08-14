package com.example.product_service.dto.request.variant;

import com.example.product_service.dto.validation.AtLeastOneFieldNotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductVariantRequest {
    @Min(value = 0, message = "{Min}")
    @Max(value = 100000000, message = "{Max}")
    private Integer price;
    @Min(value = 1, message = "{Min}")
    private int stockQuantity;
    @Min(value = 0, message = "{Min}")
    @Max(value = 100, message = "{Max}")
    private int discountRate;
    private List<Long> optionValueIds;
}
