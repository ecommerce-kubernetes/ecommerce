package com.example.product_service.product.adapter.in.web.dto.request;

import com.example.product_service.product.application.service.dto.command.RegisterProductOptionCommand;
import lombok.Builder;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.List;

@Builder
public record RegisterProductOptionRequest(
        @UniqueElements(message = "{product.options.optionTypeIds.unique}")
        List<Long> optionTypeIds
) {
    public RegisterProductOptionCommand toCommand(Long productId) {
        return RegisterProductOptionCommand.builder()
                .productId(productId)
                .optionTypeIds(optionTypeIds)
                .build();
    }
}
