package com.example.product_service.option.adapter.in.web.dto.request;

import com.example.product_service.option.application.service.dto.command.OptionCommand;
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
    public OptionCommand.Create toCommand() {
        List<String> valueNames = mappingValueNames(values);
        return OptionCommand.Create.builder()
                .name(name)
                .valueNames(valueNames)
                .build();
    }

    private List<String> mappingValueNames(List<OptionValueRequest> values) {
        return values.stream().map(OptionValueRequest::name).toList();
    }
}
