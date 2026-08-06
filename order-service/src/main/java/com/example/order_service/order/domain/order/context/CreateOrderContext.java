package com.example.order_service.order.domain.order.context;

import com.example.order_service.order.domain.order.AppliedCartCoupon;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.Builder;
import org.springframework.util.Assert;

import java.util.List;

@Builder
public record CreateOrderContext(
        Orderer orderer,
        ShippingAddress shippingAddress,
        List<CreateOrderItemContext> items,
        AppliedCartCoupon appliedCartCoupon,
        OrderAmount orderAmount
) {
    public CreateOrderContext {
        Assert.notNull(orderer, "주문(Order) 생성시 주문자는 필수이다.");
        Assert.notNull(shippingAddress, "주문(Order) 생성시 배송 정보는 필수이다.");
        Assert.notNull(items, "주문(Order) 생성시 주문 항목은 필수이다.");
        Assert.notNull(orderAmount, "주문(Order) 생성시 주문 가격 정보는 필수이다.");
    }
}
