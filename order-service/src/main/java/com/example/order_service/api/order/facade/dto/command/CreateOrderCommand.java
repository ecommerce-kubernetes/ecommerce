package com.example.order_service.api.order.facade.dto.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
}
