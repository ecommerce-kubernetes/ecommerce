package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.domain.model.vo.CouponInfo;
import com.example.order_service.api.order.domain.model.vo.OrderPriceDetail;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderCreationContext {
    private Orderer orderer;
    private OrderPriceDetail orderPriceDetail;
    private CouponInfo couponInfo;
    private List<OrderItemCreationContext> orderItemCreationContexts;
    private String deliveryAddress;

    public static OrderCreationContext of(Orderer orderer, OrderPriceDetail orderPriceDetail, CouponInfo couponInfo, List<OrderItemCreationContext> orderItemCreationContexts,
                                          String deliveryAddress) {
        return OrderCreationContext.builder()
                .orderer(orderer)
                .orderPriceDetail(orderPriceDetail)
                .couponInfo(couponInfo)
                .orderItemCreationContexts(orderItemCreationContexts)
                .deliveryAddress(deliveryAddress)
                .build();
    }
}
