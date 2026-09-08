package com.example.product_service.option.application.service.dto.result;

import com.example.product_service.option.domain.OptionType;
import com.example.product_service.option.domain.OptionValue;
import lombok.Builder;

import java.util.List;

@Builder
public record OptionTypeResult(
        Long id,
        String name,
        List<OptionValueResult> values
) {

    @Builder
    public record OptionValueResult(
            Long id,
            String name
    ) {

        public static OptionValueResult from(OptionValue optionValue) {
            return OptionValueResult.builder()
                    .id(optionValue.getId())
                    .name(optionValue.getName())
                    .build();
        }

        public static List<OptionValueResult> from(List<OptionValue> optionValues) {
            return optionValues.stream().map(OptionValueResult::from).toList();
        }
    }

    public static OptionTypeResult from(OptionType optionType) {
        List<OptionValueResult> values = OptionValueResult.from(optionType.getOptionValues());
        return OptionTypeResult.builder()
                .id(optionType.getId())
                .name(optionType.getName())
                .values(values)
                .build();
    }
}
