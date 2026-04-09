package com.example.product_service.api.option.service.dto.command;

import lombok.Builder;

import java.util.List;

public class OptionCommand {
    @Builder
    public record Create (
            String name,
            List<String> valueNames
    ) {}

    @Builder
    public record UpdateOptionType (
            Long id,
            String name
    ) {}

    @Builder
    public record UpdateOptionValue (
            Long id,
            String name
    ) {}
}
