package com.example.product_service.api.option.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

public class OptionRequest {

    @Builder
    public record CreateRequest (
            @NotBlank(message = "옵션 이름은 필수 입니다")
            String name,

            @NotEmpty(message = "최소 1개의 옵션 값을 입력해야합니다")
            @UniqueElements(message = "옵션값은 중복될 수 없습니다")
            List<OptionValueRequest> values
    ) { }

    @Builder
    public record OptionValueRequest(
            @NotBlank(message = "옵션 값 이름은 필수 입니다")
            String name
    ) { }

    @Builder
    public record UpdateRequest (
            @NotBlank(message = "이름은 필수입니다")
            String name

    ) { }
}
