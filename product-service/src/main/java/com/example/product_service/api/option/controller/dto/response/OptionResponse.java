package com.example.product_service.api.option.controller.dto.response;

import com.example.product_service.api.option.service.dto.result.OptionResult;
import com.example.product_service.api.option.service.dto.result.OptionValueResult;
import lombok.Builder;

import java.util.List;

public class OptionResponse {

    @Builder
    public record Detail(
            Long id,
            String name,
            List<Value> values
    ) {
        public static Detail from (OptionResult result) {
            List<Value> valueResponses = mappingValues(result.getValues());
            return Detail.builder()
                    .id(result.getId())
                    .name(result.getName())
                    .values(valueResponses)
                    .build();
        }

        private static List<Value> mappingValues(List<OptionValueResult> values) {
            return values.stream().map(Value::from).toList();
        }

        public static List<Detail> from(List<OptionResult> results) {
            return results.stream().map(Detail::from).toList();
        }
    }

    @Builder
    public record Value (
            Long id,
            String name
    ) {
        public static Value from(OptionValueResult result) {
            return Value.builder()
                    .id(result.getId())
                    .name(result.getName())
                    .build();
        }
    }
}
