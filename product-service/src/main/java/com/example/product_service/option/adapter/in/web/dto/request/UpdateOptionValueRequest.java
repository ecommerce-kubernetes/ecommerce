package com.example.product_service.option.adapter.in.web.dto.request;

import com.example.product_service.option.application.service.dto.command.OptionCommand;
import com.example.product_service.option.application.service.dto.command.UpdateOptionValueCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateOptionValueRequest(
        @NotBlank(message = "{option-value.name.notBlank}")
        String name
) {

    public UpdateOptionValueCommand toCommand(Long optionTypeId, Long optionValueId) {
        return UpdateOptionValueCommand.builder()
                .optionTypeId(optionTypeId)
                .optionValueId(optionValueId)
                .name(name)
                .build();
    }
}
