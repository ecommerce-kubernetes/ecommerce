package com.example.order_service.order.application.dto.command;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class CreateOrderCommand {
    private Long userId;
    private List<CreateOrderItemCommand> orderItemCommands;
    private String deliveryAddress;
    private Long couponId;
    private Long pointToUse;
    private Long expectedPrice;

    public static CreateOrderCommand of(Long userId, List<CreateOrderItemCommand> orderItemDtoList,
                                        String deliveryAddress, Long couponId, Long pointToUse, Long expectedPrice){
        return CreateOrderCommand.builder()
                .userId(userId)
                .orderItemCommands(orderItemDtoList)
                .deliveryAddress(deliveryAddress)
                .couponId(couponId)
                .pointToUse(pointToUse)
                .expectedPrice(expectedPrice)
                .build();
    }

    public List<Long> getProductVariantIds() {
        return orderItemCommands.stream().map(CreateOrderItemCommand::getProductVariantId).toList();
    }

    public Map<Long, Integer> getQuantityMap() {
        return orderItemCommands.stream().collect(Collectors.toMap(CreateOrderItemCommand::getProductVariantId, CreateOrderItemCommand::getQuantity));
    }
}
