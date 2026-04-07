package com.example.product_service.api.option.controller.dto.request;

import com.example.product_service.api.option.service.dto.command.OptionCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public class OptionRequest {

    @Builder
    public record Create(
            @NotBlank(message = "옵션 이름은 필수 입니다")
            String name,

            @NotEmpty(message = "최소 1개의 옵션 값을 입력해야합니다")
            @UniqueElements(message = "옵션값은 중복될 수 없습니다")
            List<Value> values
    ) {
        public OptionCommand.Create toCommand() {
            List<String> valueNames = mappingValueNames(values);
            return OptionCommand.Create.builder()
                    .name(name)
                    .valueNames(valueNames)
                    .build();
        }

        private List<String> mappingValueNames(List<Value> values) {
            return values.stream().map(Value::name).toList();
        }
    }

    @Builder
    public record Value(
            @NotBlank(message = "옵션 값 이름은 필수 입니다")
            String name
    ) { }

    @Builder
    public record UpdateOptionType(
            @NotBlank(message = "이름은 필수입니다")
            String name
    ) {
        public OptionCommand.UpdateOptionType toCommand() {
            return OptionCommand.UpdateOptionType.builder()
                    .name(name)
                    .build();
        }
    }

    @Builder
    public record UpdateOptionValue(
            @NotBlank(message = "이름은 필수입니다")
            String name
    ) {
        public OptionCommand.UpdateOptionValue toCommand() {
            return OptionCommand.UpdateOptionValue.builder()
                    .name(name)
                    .build();
        }
    }
}
