package com.example.product_service.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductValidateResult {
    private Set<Long> requestedOptionTypeIds;
    private Map<Long, Integer> productOptionData;
    private Set<Long> allRequestedOptionValueIds;
    private Set<String> skusInRequest;
}
