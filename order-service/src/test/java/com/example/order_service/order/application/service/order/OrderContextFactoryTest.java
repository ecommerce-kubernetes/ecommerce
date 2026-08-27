package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.order.AppliedCartCoupon;
import com.example.order_service.order.domain.order.AppliedItemCoupon;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.ordersheet.*;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderContextFactoryTest {

    private final OrderContextFactory factory = new OrderContextFactory();

    @Test
    @DisplayName("주문 생성 컨텍스트를 생성한다.")
    void create() {
        //given
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호");

        CouponDiscountPolicy couponDiscountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인", couponDiscountPolicy, 1);
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(2L, "1000원 할인", couponDiscountPolicy, Money.wons(10000L));
        OrderSheet orderSheet = OrderSheetFixtureBuilder.given()
                .withShippingAddress(shippingAddress)
                .withItemCoupon(itemCoupon)
                .withCartCoupon(cartCoupon)
                .withUsedPoint(Money.wons(1000L))
                .build();

        AppliedCartCoupon appliedCartCoupon = AppliedCartCoupon.of(2L, "1000원 할인");
        OrderAmount orderAmount = OrderAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(1000L),
                Money.wons(24000L)
        );

        AppliedItemCoupon appliedItemCoupon = AppliedItemCoupon.of(1L, "1000원 할인");

        OrderItemAmount orderItemAmount = OrderItemAmount.of(
                Money.wons(30000L),
                Money.wons(3000L),
                Money.wons(27000L),
                Money.wons(1000L),
                Money.wons(26000L)
        );

        //when
        CreateOrderContext context = factory.create(orderSheet);
        //then
        assertThat(context.orderer()).isEqualTo(orderSheet.getOrderer());
        assertThat(context.shippingAddress()).isEqualTo(orderSheet.getShippingAddress());
        assertThat(context.appliedCartCoupon()).isEqualTo(appliedCartCoupon);
        assertThat(context.orderAmount()).isEqualTo(orderAmount);

        OrderSheetItem orderSheetItem = orderSheet.getItems().getFirst();
        assertThat(context.items())
                .extracting("productSnapshot", "priceSnapshot", "appliedItemCoupon", "quantity", "options", "orderItemAmount")
                .containsExactly(
                        tuple(orderSheetItem.getProductSnapshot(), orderSheetItem.getPriceSnapshot(),
                                appliedItemCoupon, orderSheetItem.getQuantity(), orderSheetItem.getOptionSnapshots() ,orderItemAmount)
                );
    }
}