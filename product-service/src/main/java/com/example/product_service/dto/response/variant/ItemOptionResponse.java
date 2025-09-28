package com.example.product_service.dto.response.variant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ItemOptionResponse {
    private String optionTypeName;
    private String optionValueName;
}
