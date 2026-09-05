package com.example.order_service.cart.application.dto.result;

import com.example.order_service.cart.application.external.dto.CartProductResult;
import lombok.Builder;

import java.util.List;

@Builder
public record CartItemOption(
        String optionTypeName,
        String optionValueName
) {
    public static CartItemOption of(String optionTypeName, String optionValueName) {
        return CartItemOption.builder()
                .optionTypeName(optionTypeName)
                .optionValueName(optionValueName)
                .build();
    }

    public static List<CartItemOption> from(List<CartProductResult.ProductOption> options) {
        return options.stream().map(option -> CartItemOption.of(option.optionTypeName(), option.optionValueName()))
                .toList();
    }
}
