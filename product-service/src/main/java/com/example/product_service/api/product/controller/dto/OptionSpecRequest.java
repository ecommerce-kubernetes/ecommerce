package com.example.product_service.api.product.controller.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionSpecRequest {
    private List<Long> optionTypeIds;
}
