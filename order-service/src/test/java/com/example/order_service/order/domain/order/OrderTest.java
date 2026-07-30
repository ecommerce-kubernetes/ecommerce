package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class OrderTest {

    @Test
    @DisplayName("주문을 생성할때 주문자가 누락되면 예외가 발생한다.")
    void create_orderer_null() {
        //given
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");
        CreateOrderItemContext orderItemContext = createOrderItemContext();
        //when
        //then
    }

    @Test
    @DisplayName("주문을 생성할때 배송 정보가 누락되면 예외가 발생한다.")
    void create_shippingAddress_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문을 생성할때 주문 가격 정보가 누락되면 예외가 발생한다.")
    void create_orderAmount_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문을 생성할때 주문 항목은 비어있을 수 없다")
    void create_orderItems_empty() {
        //given
        //when
        //then
    }

    private CreateOrderItemContext createOrderItemContext() {
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "SKU", "상품", "/product/product.jpg");
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10, Money.wons(1000L), Money.wons(9000L));
        AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(1L, "1000원 상품 할인 쿠폰");
        int quantity = 3;
        OrderItemAmount orderItemAmount = OrderItemAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(27000L), Money.wons(1000L), Money.wons(26000L));
        return CreateOrderItemContext.builder()
                .productSnapshot(productSnapshot)
                .priceSnapshot(priceSnapshot)
                .appliedItemCoupon(appliedItemCoupon)
                .quantity(quantity)
                .options(Collections.emptyList())
                .orderItemAmount(orderItemAmount)
                .build();
    }
}