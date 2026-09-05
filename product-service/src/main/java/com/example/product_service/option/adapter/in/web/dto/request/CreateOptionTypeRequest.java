package com.example.product_service.option.adapter.in.web.dto.request;

import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Builder
public record CreateOptionTypeRequest(
        @NotBlank(message = "{option-type.name.notBlank}")
        String name,

        @Valid
        @NotEmpty(message = "{option-type.values.notEmpty}")
        @UniqueElements(message = "{option-type.values.unique}")
        List<CreateOptionValueRequest> values
) {

    @Builder
    public record CreateOptionValueRequest(
            @NotBlank(message = "{option-value.name.notBlank}")
            String name
    ) {
        public CreateOptionTypeCommand.CreateOptionValueCommand toCommand() {
            return CreateOptionTypeCommand.CreateOptionValueCommand.builder()
                    .name(name)
                    .build();
        }
    }

    public CreateOptionTypeCommand toCommand() {
        List<CreateOptionTypeCommand.CreateOptionValueCommand> valueCommands = values.stream().map(CreateOptionValueRequest::toCommand).toList();

        return CreateOptionTypeCommand.builder()
                .name(name)
                .values(valueCommands)
                .build();
    }
}
