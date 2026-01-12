package com.example.product_service.api.product.service.dto.result;

import lombok.Getter;

import java.util.List;

@Getter
public class OptionSpecResponse {
    private Long productId;
    private List<ProductOptionSpecResponse> optionNames;

    public static class ProductOptionSpecResponse {
        private Long optionTypeId;
        private String name;
        private Integer priority;
    }
}
