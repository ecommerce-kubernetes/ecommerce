package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.OrderPriceInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateOrderCommand {
    private Long userId;
    private List<CreateOrderItemCommand> itemCommands;
    private OrderPriceInfo orderPriceInfo;
    private AppliedCoupon appliedCoupon;
    private String deliveryAddress;

    @Builder
    private CreateOrderCommand(Long userId, List<CreateOrderItemCommand> itemCommands, OrderPriceInfo orderPriceInfo,
                               AppliedCoupon appliedCoupon, String deliveryAddress){
        this.userId = userId;
        this.itemCommands = itemCommands;
        this.orderPriceInfo = orderPriceInfo;
        this.appliedCoupon = appliedCoupon;
        this.deliveryAddress = deliveryAddress;
    }
}
