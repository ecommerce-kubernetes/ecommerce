package com.example.product_service.api.option.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionRequest {

    @NotBlank(message = "옵션 이름은 필수 입니다")
    private String name;

    @UniqueElements(message = "옵션값은 중복될 수 없습니다")
    private List<String> values = new ArrayList<>();

    @Builder
    private OptionRequest(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }
}
