package com.example.order_service.order.application.service.ordersheet.dto.command;

import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
public record CreateDirectOrderSheetCommand(
        Long userId,
        List<OrderVariant> items
) {

    public List<Long> toItemVariantIds(){
        return items.stream().map(OrderVariant::productVariantId).toList();
    }

    @Builder
    public record OrderVariant (
            Long productVariantId,
            Integer quantity
    ) {}
}
