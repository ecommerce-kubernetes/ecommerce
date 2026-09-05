package com.example.product_service.option.fixture;

import com.example.product_service.option.application.service.dto.result.OptionTypeResult;
import com.example.product_service.option.application.service.dto.result.OptionTypesResult;

import java.util.List;

public class OptionResultFixture {
    public static OptionTypesResult.OptionTypesResultBuilder anOptionTypesResult() {
        OptionTypeResult color = anOptionTypeResult().build();
        return OptionTypesResult.builder()
                .types(List.of(color));
    }

    public static OptionTypeResult.OptionTypeResultBuilder anOptionTypeResult() {
        OptionTypeResult.OptionValueResult blue = OptionTypeResult.OptionValueResult.builder()
                .id(2L)
                .name("BLUE")
                .build();

        OptionTypeResult.OptionValueResult red = OptionTypeResult.OptionValueResult.builder()
                .id(3L)
                .name("RED")
                .build();

        return OptionTypeResult.builder()
                .id(1L)
                .name("색상")
                .values(List.of(blue, red));
    }
}
