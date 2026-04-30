package com.example.order_service.cart.api.dto.request;

import com.example.order_service.cart.application.dto.command.CartCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public class CartRequest {

    @Builder
    public record AddItems (
            @Valid
            @NotEmpty(message = "장바구니에 추가할 상품이 하나 이상 있어야 합니다.")
            List<Item> items
    ) {
        public CartCommand.AddItems toCommand(Long userId) {
            return CartCommand.AddItems.builder()
                    .userId(userId)
                    .items(mapToCartCommandItem(items))
                    .build();
        }

        private List<CartCommand.Item> mapToCartCommandItem(List<Item> items) {
            return  items.stream().map(Item::toCommand).toList();
        }
    }

    @Builder(toBuilder = true)
    public record Item (
            @NotNull(message = "productVariantId는 필수값입니다")
            Long productVariantId,
            @NotNull(message = "quantity는 필수값입니다")
            @Min(value = 1, message = "quantity는 1이상 이여야 합니다")
            Integer quantity
    ) {
        public CartCommand.Item toCommand() {
            return CartCommand.Item.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }

    @Builder
    public record UpdateQuantity(
            @NotNull(message = "수량은 필수값 입니다")
            @Min(value = 1, message = "수량은 1이상이여야 합니다")
            Integer quantity
    ) {
        public CartCommand.UpdateQuantity toCommand(Long userId, Long cartItemId) {
            return CartCommand.UpdateQuantity.builder()
                    .userId(userId)
                    .cartItemId(cartItemId)
                    .quantity(quantity)
                    .build();
        }
    }
}
