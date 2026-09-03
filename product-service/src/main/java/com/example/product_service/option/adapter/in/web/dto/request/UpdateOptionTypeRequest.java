package com.example.product_service.option.adapter.in.web.dto.request;

import com.example.product_service.option.application.service.dto.command.OptionCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UpdateOptionTypeRequest(
        @NotBlank(message = "이름은 필수입니다")
        String name
) {
    public OptionCommand.UpdateOptionType toCommand() {
        return OptionCommand.UpdateOptionType.builder()
                .name(name)
                .build();
    }
}
