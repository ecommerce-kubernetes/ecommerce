package com.example.order_service.api.order.application.event;

import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreatedEvent {
    private Long userId;
    private List<Long> orderedVariantIds;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderCreatedEvent(Long userId, List<Long> orderedVariantIds) {
        this.userId = userId;
        this.orderedVariantIds = orderedVariantIds;
    }

    private static OrderCreatedEvent of(Long userId, List<Long> orderedVariantIds) {
        return OrderCreatedEvent.builder()
                .userId(userId)
                .orderedVariantIds(orderedVariantIds)
                .build();
    }

    public static OrderCreatedEvent from(OrderCreationResult result) {
        List<Long> orderedVariantIds = result.getOrderItemDtoList().stream().map(OrderItemDto::getProductVariantId).toList();
        return of(result.getUserId(), orderedVariantIds);
    }
}
