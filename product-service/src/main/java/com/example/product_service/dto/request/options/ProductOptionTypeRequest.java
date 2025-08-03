package com.example.product_service.dto.request.options;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionTypeRequest {
    private Long optionTypeId;
    private int priority;
}
