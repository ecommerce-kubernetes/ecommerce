package com.example.order_service.order.infrastructure.messaging.dto;

import com.example.order_service.order.application.messaging.dto.SagaMessage;
import com.example.order_service.order.domain.vo.SagaPayload;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InventoryMessage {
    private SagaType type;
    private String orderNo;
    private List<Item> items;

    @Builder
    public record Item(
            Long productVariantId,
            Integer quantity
    ) {
        public static Item from(SagaPayload.ItemPayload payload) {
            return Item.builder()
                    .productVariantId(payload.getProductVariantId())
                    .quantity(payload.getQuantity())
                    .build();
        }

        public static List<Item> from(List<SagaPayload.ItemPayload> payloads) {
            return payloads.stream().map(Item::from).toList();
        }
    }

    public static InventoryMessage deduct(SagaMessage message) {
        return InventoryMessage.builder()
                .type(SagaType.DEDUCT_INVENTORY)
                .orderNo(message.getOrderNo())
                .items(Item.from(message.getPayload().getItems()))
                .build();
    }

    public static InventoryMessage restore(SagaMessage message) {
        return InventoryMessage.builder()
                .type(SagaType.RESTORE_INVENTORY)
                .orderNo(message.getOrderNo())
                .items(Item.from(message.getPayload().getItems()))
                .build();
    }
}
