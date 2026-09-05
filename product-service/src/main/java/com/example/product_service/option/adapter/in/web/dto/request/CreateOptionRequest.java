package com.example.product_service.option.adapter.in.web.dto.request;

import com.example.product_service.option.application.service.dto.command.CreateOptionTypeCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Builder
public record CreateOptionRequest(
        @NotBlank(message = "옵션 이름은 필수 입니다")
        String name,

        @NotEmpty(message = "최소 1개의 옵션 값을 입력해야합니다")
        @UniqueElements(message = "옵션값은 중복될 수 없습니다")
        List<OptionValueRequest> values
) {

    @Builder
    public record OptionValueRequest(
            @NotBlank(message = "옵션 값 이름은 필수 입니다")
            String name
    ) {
        public CreateOptionTypeCommand.CreateOptionValueCommand toCommand() {
            return CreateOptionTypeCommand.CreateOptionValueCommand.builder()
                    .name(name)
                    .build();
        }
    }

    public CreateOptionTypeCommand toCommand() {
        List<CreateOptionTypeCommand.CreateOptionValueCommand> valueCommands = values.stream().map(OptionValueRequest::toCommand).toList();

        return CreateOptionTypeCommand.builder()
                .name(name)
                .values(valueCommands)
                .build();
    }
}
