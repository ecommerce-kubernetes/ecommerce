package com.example.product_service.api.option.service.dto.result;

import com.example.product_service.api.option.domain.model.OptionValue;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OptionValueResult {
    private Long id;
    private String name;

    @Builder
    private OptionValueResult(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static OptionValueResult from(OptionValue optionValue) {
        return OptionValueResult.builder()
                .id(optionValue.getId())
                .name(optionValue.getName())
                .build();
    }
}
