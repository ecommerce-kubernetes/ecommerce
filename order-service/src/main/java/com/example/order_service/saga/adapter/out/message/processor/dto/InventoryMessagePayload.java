package com.example.order_service.saga.adapter.out.message.processor.dto;

import com.example.order_service.saga.domain.OrderSagaPayload;
import com.example.order_service.saga.domain.event.ReduceInventoryEvent;
import com.example.order_service.saga.domain.event.RestoreInventoryEvent;
import lombok.Builder;

import java.util.List;

@Builder
public record InventoryMessagePayload(
        Long executionId,
        List<Item> items
) {

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity
    ) {

        public static Item from(OrderSagaPayload.OrderLine orderLine) {
            return Item.builder()
                    .productVariantId(orderLine.productVariantId())
                    .quantity(orderLine.quantity())
                    .build();
        }

        public static List<Item> from(List<OrderSagaPayload.OrderLine> orderLines) {
            return orderLines.stream().map(Item::from).toList();
        }
    }

    public static InventoryMessagePayload from(ReduceInventoryEvent event) {
        List<Item> items = Item.from(event.orderLines());
        return InventoryMessagePayload.builder()
                .executionId(event.executionId())
                .items(items)
                .build();
    }

    public static InventoryMessagePayload from(RestoreInventoryEvent event) {
        List<Item> items = Item.from(event.orderLines());
        return InventoryMessagePayload.builder()
                .executionId(event.executionId())
                .items(items)
                .build();
    }
}
