package com.example.product_service.api.option.service.dto.result;

import com.example.product_service.api.option.domain.model.OptionType;
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
