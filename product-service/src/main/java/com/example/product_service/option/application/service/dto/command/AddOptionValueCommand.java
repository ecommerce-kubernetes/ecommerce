package com.example.product_service.option.application.service.dto.command;

import lombok.Builder;

@Builder
public record AddOptionValueCommand(
        Long optionTypeId,
        String name
) {
}
