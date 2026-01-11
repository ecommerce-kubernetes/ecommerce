package com.example.product_service.api.option.service.dto;

import com.example.product_service.api.option.domain.OptionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OptionResponse {
    private Long id;
    private String name;
    private List<OptionValueResponse> values;

    @Builder
    private OptionResponse(Long id, String name, List<OptionValueResponse> values) {
        this.id = id;
        this.name = name;
        this.values = values;
    }

    public static OptionResponse from(OptionType optionType) {
        return OptionResponse.builder()
                .id(optionType.getId())
                .name(optionType.getName())
                .values(optionType.getOptionValues().stream().map(OptionValueResponse::from).toList())
                .build();
    }
}
