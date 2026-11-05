package com.example.order_service.cart.api.dto.request;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record AddCartItemsRequest(

        @Valid
        @Size(min = 1, max = 20, message = "{cart.items.size}")
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
            @NotNull(message = "{cart.item.productVariantId.notNull}")
            Long productVariantId,
            @NotNull(message = "{cart.item.quantity.notNull}")
            @Min(value = 1, message = "{cart.item.quantity.min}")
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
