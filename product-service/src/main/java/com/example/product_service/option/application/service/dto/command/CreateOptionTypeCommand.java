package com.example.product_service.option.application.service.dto.command;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateOptionTypeCommand(
        String name,
        List<CreateOptionValueCommand> values
) {

    @Builder
    public record CreateOptionValueCommand(
            String name
    ) {
    }
}
