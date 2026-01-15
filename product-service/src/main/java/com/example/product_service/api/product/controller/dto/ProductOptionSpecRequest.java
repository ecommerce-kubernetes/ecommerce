package com.example.product_service.api.product.controller.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductOptionSpecRequest {
    @NotNull(message = "옵션 id 리스트는 필수 입니다")
    @Size(max = 3, message = "옵션은 최대 3개까지만 설정 가능합니다")
    @UniqueElements(message = "중복된 옵션 종류가 포함되어 있습니다")
    private List<Long> optionTypeIds;

    @Builder
    private ProductOptionSpecRequest(List<Long> optionTypeIds) {
        this.optionTypeIds = optionTypeIds;
    }
}
