package com.example.order_service.order.application.service.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.application.port.dto.OrderCouponStatus;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;

import java.time.LocalDateTime;
import java.util.List;

public class OrderCouponResultFixture {

    public static ItemCouponsResult.ItemCouponsResultBuilder anItemCoupons() {
        ItemCouponsResult.ItemCouponResult itemCouponResult = anItemCoupon().build();
        return ItemCouponsResult.builder()
                .userId(1L)
                .itemCoupons(List.of(itemCouponResult));
    }

    public static ItemCouponsResult.ItemCouponResult.ItemCouponResultBuilder anItemCoupon() {
        CouponDiscountPolicy discountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        ItemCouponSnapshot itemCoupon = ItemCouponSnapshot.of(1L, "1000원 할인 쿠폰", discountPolicy, 1);
        return ItemCouponsResult.ItemCouponResult.builder()
                .status(OrderCouponStatus.AVAILABLE)
                .itemCoupon(itemCoupon)
                .expiresAt(LocalDateTime.now().plusDays(10));
    }

    public static CartCouponResult.CartCouponResultBuilder anCartCoupon() {
        CouponDiscountPolicy discountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCoupon = CartCouponSnapshot.of(1L, "1000원 할인 쿠폰", discountPolicy, Money.wons(10000L));
        return CartCouponResult.builder()
                .status(OrderCouponStatus.AVAILABLE)
                .cartCoupon(cartCoupon)
                .expiresAt(LocalDateTime.now().plusDays(10));
    }
}
