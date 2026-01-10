package com.example.product_service.api.option.service.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OptionValueResponse {
    private Long id;
    private String value;

    @Builder
    private OptionValueResponse(Long id, String value) {
        this.id = id;
        this.value = value;
    }
}
