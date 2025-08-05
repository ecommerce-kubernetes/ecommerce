package com.example.product_service.dto.request.variant;

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
    private int price;
    private int stockQuantity;
    private int discountRate;
    private List<Long> optionValueIds;
}
