package com.example.product_service.service.dto;

import com.example.product_service.dto.request.variant.ProductVariantRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductValidateResult {
    private Map<Long, Integer> productOptionData;
    private Map<String, ProductVariantRequest> productVariants;
    private Map<Long, Set<Long>> optionTypeToOptionValueIds;
}
