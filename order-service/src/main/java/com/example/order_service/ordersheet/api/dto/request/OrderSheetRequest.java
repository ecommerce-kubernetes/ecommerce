package com.example.order_service.ordersheet.api.dto.request;

import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public class OrderSheetRequest {

    @Builder
    public record Create(
            @Valid
            @NotEmpty(message = "주문 상품은 한개 이상이여야 합니다")
            List<OrderItem> items
    ) {

        public OrderSheetCommand.Create toCommand(Long userId) {
            return OrderSheetCommand.Create.builder()
                    .userId(userId)
                    .items(itemsMapToCommand(items))
                    .build();
        }

        private List<OrderSheetCommand.OrderItem> itemsMapToCommand(List<OrderItem> items) {
            return items.stream().map(OrderItem::toCommand).toList();
        }
    }

    @Builder(toBuilder = true)
    public record OrderItem(
            @NotNull(message = "productVariantId는 필수값입니다")
            Long productVariantId,
            @NotNull(message = "quantity는 필수값입니다")
            @Min(value = 1, message = "quantity는 1이상 이여야 합니다")
            Integer quantity
    ) {
        public OrderSheetCommand.OrderItem toCommand() {
            return OrderSheetCommand.OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }
}
