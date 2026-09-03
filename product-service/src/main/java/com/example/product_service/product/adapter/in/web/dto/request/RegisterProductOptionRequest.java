package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.ProductCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Builder
public record RegisterProductOptionRequest(
        @Valid
        @NotNull(message = "옵션 리스트는 필수 입니다")
        @Size(max = 3, message = "옵션은 최대 3개까지만 설정 가능합니다")
        @UniqueElements(message = "옵션 ID는 중복될 수 없습니다")
        List<Long> optionTypeIds
) {
    public ProductCommand.OptionRegister toCommand(Long productId) {
        return ProductCommand.OptionRegister.builder()
                .productId(productId)
                .optionTypeIds(optionTypeIds)
                .build();
    }
}
