package com.example.product_service.option.adapter.in.web.dto.request;

import com.example.product_service.option.application.service.dto.command.UpdateOptionTypeCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateOptionTypeRequest(
        @NotBlank(message = "{option-type.name.notBlank}")
        String name
) {
    public UpdateOptionTypeCommand toCommand(Long optionTypeId) {
        return UpdateOptionTypeCommand.builder()
                .optionTypeId(optionTypeId)
                .name(name)
                .build();
    }
}
