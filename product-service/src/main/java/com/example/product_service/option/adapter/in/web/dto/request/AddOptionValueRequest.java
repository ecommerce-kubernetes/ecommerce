package com.example.product_service.option.adapter.in.web.dto.request;

import com.example.product_service.option.application.service.dto.command.AddOptionValueCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AddOptionValueRequest(
        @NotBlank(message = "{option-value.name.notBlank}")
        String name
) {

    public AddOptionValueCommand toCommand(Long optionTypeId) {
        return AddOptionValueCommand.builder()
                .optionTypeId(optionTypeId)
                .name(name)
                .build();
    }
}
