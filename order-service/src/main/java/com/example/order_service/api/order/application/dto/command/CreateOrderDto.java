package com.example.order_service.api.order.application.dto.command;

import com.example.order_service.api.common.security.principal.UserPrincipal;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderDto {
    private UserPrincipal userPrincipal;
    private List<CreateOrderItemDto> orderItemDtoList;
    private String deliveryAddress;
    private Long couponId;
    private Long pointToUse;
    private Long expectedPrice;

    @Builder
    private CreateOrderDto(UserPrincipal userPrincipal, List<CreateOrderItemDto> orderItemDtoList,
                           String deliveryAddress, Long couponId, Long pointToUse, Long expectedPrice){
        this.userPrincipal = userPrincipal;
        this.orderItemDtoList = orderItemDtoList;
        this.deliveryAddress = deliveryAddress;
        this.couponId = couponId;
        this.pointToUse = pointToUse;
        this.expectedPrice = expectedPrice;
    }

    public static CreateOrderDto of(UserPrincipal userPrincipal, CreateOrderRequest createOrderRequest){
        List<CreateOrderItemDto> orderItems = createOrderRequest.getItems().stream().map(item ->
                CreateOrderItemDto.of(item.getProductVariantId(), item.getQuantity())).toList();
        return of(userPrincipal, orderItems, createOrderRequest.getDeliveryAddress(), createOrderRequest.getCouponId(),
                createOrderRequest.getPointToUse(), createOrderRequest.getExpectedPrice());
    }

    public static CreateOrderDto of(UserPrincipal userPrincipal, List<CreateOrderItemDto> orderItemDtoList,
                                    String deliveryAddress, Long couponId, Long pointToUse, Long expectedPrice){
        return CreateOrderDto.builder()
                .userPrincipal(userPrincipal)
                .orderItemDtoList(orderItemDtoList)
                .deliveryAddress(deliveryAddress)
                .couponId(couponId)
                .pointToUse(pointToUse)
                .expectedPrice(expectedPrice)
                .build();
    }
}
