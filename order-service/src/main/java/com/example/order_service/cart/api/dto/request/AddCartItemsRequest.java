package com.example.order_service.cart.api.dto.request;

import com.example.order_service.cart.application.service.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.service.dto.command.CartCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsRequest(

        @Valid
        @NotEmpty(message = "장바구니에 추가할 상품이 하나 이상 있어야 합니다.")
        List<Item> items
) {
    public AddCartItemsCommand toCommand(Long userId) {
        return AddCartItemsCommand.builder()
                .userId(userId)
                .items(mapToCartCommandItem(items))
                .build();
    }

    private List<AddCartItemsCommand.Item> mapToCartCommandItem(List<Item> items) {
        return items.stream().map(Item::toCommand).toList();
    }

    @Builder
    public record Item(
            @NotNull(message = "productVariantId는 필수값입니다")
            Long productVariantId,
            @NotNull(message = "quantity는 필수값입니다")
            @Min(value = 1, message = "quantity는 1이상 이여야 합니다")
            Integer quantity
    ) {
        public AddCartItemsCommand.Item toCommand() {
            return AddCartItemsCommand.Item.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }
}
