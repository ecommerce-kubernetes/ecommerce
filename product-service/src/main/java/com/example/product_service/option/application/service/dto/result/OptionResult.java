package com.example.product_service.option.application.service.dto.result;

import com.example.product_service.option.domain.model.OptionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OptionResult {
    private Long id;
    private String name;
    private List<OptionValueResult> values;

    @Builder
    private OptionResult(Long id, String name, List<OptionValueResult> values) {
        this.id = id;
        this.name = name;
        this.values = values;
    }

    public static OptionResult from(OptionType optionType) {
        return OptionResult.builder()
                .id(optionType.getId())
                .name(optionType.getName())
                .values(optionType.getOptionValues().stream().map(OptionValueResult::from).toList())
                .build();
    }
}
