package com.example.order_service.order.domain.order.context;

import com.example.order_service.order.domain.order.AppliedItemCoupon;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import lombok.Builder;
import org.springframework.util.Assert;

import java.util.List;

@Builder
public record CreateOrderItemContext(
        ProductSnapshot productSnapshot,
        ProductPriceSnapshot priceSnapshot,
        AppliedItemCoupon appliedItemCoupon,
        int quantity,
        List<ProductOptionSnapshot> options,
        OrderItemAmount orderItemAmount
) {
    public CreateOrderItemContext {
        Assert.notNull(productSnapshot, "주문 항목(OrderItem) 생성시 상품 정보는 필수이다.");
        Assert.notNull(priceSnapshot, "주문 항목(OrderItem) 생성시 상품 가격 정보는 필수이다.");
        Assert.notNull(quantity, "주문 항목(OrderItem) 생성시 주문 수량은 필수이다.");
        Assert.notNull(options, "주문 항목(OrderItem) 생성시 상품 옵션은 필수이다.");
        Assert.notNull(orderItemAmount, "주문 항목(OrderItem) 생성시 주문 항목 가격 정보는 필수이다.");
    }
}
