package com.example.product_service.api.option.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionRequest {

    @NotBlank(message = "옵션 이름은 필수 입니다")
    private String name;

    private List<String> values = new ArrayList<>();

    @Builder
    private OptionRequest(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }
}
