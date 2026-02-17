package com.example.product_service.api.option.controller.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonPropertyOrder({"name", "values"})
public class OptionCreateRequest {

    @NotBlank(message = "옵션 이름은 필수 입니다")
    private String name;

    @NotEmpty(message = "최소 1개의 옵션 값을 입력해야합니다")
    @UniqueElements(message = "옵션값은 중복될 수 없습니다")
    private List<String> values = new ArrayList<>();

    @Builder
    private OptionCreateRequest(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }
}
