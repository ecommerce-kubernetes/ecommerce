package com.example.order_service.api.order.facade.dto.command;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderDto {
    private Long userId;
    private List<CreateOrderItemDto> orderItemDtoList;
    private String deliveryAddress;
    private Long couponId;
    private Long pointToUse;
    private Long expectedPrice;

    @Builder
    private CreateOrderDto(Long userId, List<CreateOrderItemDto> orderItemDtoList,
                           String deliveryAddress, Long couponId, Long pointToUse, Long expectedPrice){
        this.userId = userId;
        this.orderItemDtoList = orderItemDtoList;
        this.deliveryAddress = deliveryAddress;
        this.couponId = couponId;
        this.pointToUse = pointToUse;
        this.expectedPrice = expectedPrice;
    }

    public static CreateOrderDto of(Long userId, List<CreateOrderItemDto> orderItemDtoList,
                                    String deliveryAddress, Long couponId, Long pointToUse, Long expectedPrice){
        return CreateOrderDto.builder()
                .userId(userId)
                .orderItemDtoList(orderItemDtoList)
                .deliveryAddress(deliveryAddress)
                .couponId(couponId)
                .pointToUse(pointToUse)
                .expectedPrice(expectedPrice)
                .build();
    }
}
