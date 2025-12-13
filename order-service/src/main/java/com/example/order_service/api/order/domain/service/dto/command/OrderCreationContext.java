package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderCreationContext {
    private Long userId;
    private List<OrderItemSpec> itemSpecs;
    private PriceCalculateResult priceResult;
    private String deliveryAddress;

    @Builder
    private OrderCreationContext(Long userId, List<OrderItemSpec> itemSpecs, PriceCalculateResult priceResult,
                                 String deliveryAddress){
        this.userId = userId;
        this.itemSpecs = itemSpecs;
        this.priceResult = priceResult;
        this.deliveryAddress = deliveryAddress;
    }

    public static OrderCreationContext of(Long userId, List<OrderItemSpec> itemSpecs, PriceCalculateResult priceResult, String deliveryAddress){
        return OrderCreationContext.builder()
                .userId(userId)
                .itemSpecs(itemSpecs)
                .priceResult(priceResult)
                .deliveryAddress(deliveryAddress)
                .build();

    }
}
