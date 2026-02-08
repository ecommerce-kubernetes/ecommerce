package com.example.product_service.api.option.service.dto;

import com.example.product_service.api.option.domain.model.OptionValue;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OptionValueResponse {
    private Long id;
    private String name;

    @Builder
    private OptionValueResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static OptionValueResponse from(OptionValue optionValue) {
        return OptionValueResponse.builder()
                .id(optionValue.getId())
                .name(optionValue.getName())
                .build();
    }
}
