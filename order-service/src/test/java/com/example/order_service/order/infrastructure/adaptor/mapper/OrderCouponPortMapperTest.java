package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.application.port.dto.OrderCouponStatus;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderCouponPortMapperTest {

    private final OrderCouponPortMapper orderCouponPortMapper = new OrderCouponPortMapper();

    @Test
    @DisplayName("상품 쿠폰 조회 결과를 매핑한다")
    void mapToItemCouponsResult(){
        //given
        LocalDateTime now = LocalDateTime.now();
        ItemCouponsResponse.ItemCoupon itemCoupon1 = ItemCouponsResponse.ItemCoupon.builder()
                .itemCouponId(1L)
                .status("AVAILABLE")
                .name("1000원 할인 쿠폰")
                .applyQuantityLimit(3)
                .discountType("FIXED")
                .discountAmount(1000L)
                .expiresAt(now)
                .build();

        ItemCouponsResponse.ItemCoupon itemCoupon2 = ItemCouponsResponse.ItemCoupon.builder()
                .itemCouponId(2L)
                .status("AVAILABLE")
                .name("10% 할인 쿠폰")
                .applyQuantityLimit(1)
                .discountType("RATE")
                .discountRate(10)
                .maxDiscountAmount(10000L)
                .expiresAt(now)
                .build();

        ItemCouponsResponse response = ItemCouponsResponse.builder()
                .userId(1L)
                .itemCoupons(List.of(itemCoupon1, itemCoupon2))
                .build();

        ItemCouponSnapshot expectedSnapshot1 = ItemCouponSnapshot.of(
                1L, "1000원 할인 쿠폰", new FixedCouponDiscountPolicy(Money.wons(1000L)), 3
        );
        ItemCouponSnapshot expectedSnapshot2 = ItemCouponSnapshot.of(
                2L, "10% 할인 쿠폰", new RateCouponDiscountPolicy(10, Money.wons(10000L)), 1
        );
        //when
        ItemCouponsResult result = orderCouponPortMapper.mapToItemCouponsResult(response);
        //then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.itemCoupons()).hasSize(2)
                .extracting("status", "itemCoupon", "expiresAt")
                .containsExactly(
                        tuple(OrderCouponStatus.AVAILABLE, expectedSnapshot1, now),
                        tuple(OrderCouponStatus.AVAILABLE, expectedSnapshot2, now)
                );
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회 결과를 매핑한다 (정액 쿠폰)")
    void mapToCartCouponResult_fixed(){
        //given
        CartCouponResponse response = CartCouponResponse.builder()
                .userId(1L)
                .cartCouponId(1L)
                .name("장바구니 1000원 할인 쿠폰")
                .minimumPaymentAmount(10000L)
                .discountType("FIXED")
                .discountAmount(1000L)
                .build();
        //when
        CartCouponResult result = orderCouponPortMapper.mapToCartcouponResult(response);
        //then
        CartCouponSnapshot cartCouponSnapshot = result.cartCoupon();
        assertThat(cartCouponSnapshot)
                .extracting("cartCouponId", "name", "minimumPaymentAmount")
                .containsExactly(1L, "장바구니 1000원 할인 쿠폰", Money.wons(10000L));

        assertThat(cartCouponSnapshot.getDiscountPolicy()).isInstanceOf(FixedCouponDiscountPolicy.class);
        assertThat(cartCouponSnapshot.getDiscountPolicy().calculateDiscount(Money.wons(10000L))).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회 결과를 매핑한다 (정률 쿠폰)")
    void mapToCartCouponResult_rate(){
        //given
        CartCouponResponse response = CartCouponResponse.builder()
                .userId(1L)
                .cartCouponId(1L)
                .name("장바구니 10% 할인 쿠폰")
                .minimumPaymentAmount(10000L)
                .discountType("RATE")
                .discountRate(10)
                .maxDiscountAmount(10000L)
                .build();
        //when
        CartCouponResult result = orderCouponPortMapper.mapToCartcouponResult(response);
        //then
        CartCouponSnapshot cartCouponSnapshot = result.cartCoupon();
        assertThat(cartCouponSnapshot)
                .extracting("cartCouponId", "name", "minimumPaymentAmount")
                .containsExactly(1L, "장바구니 10% 할인 쿠폰", Money.wons(10000L));

        assertThat(cartCouponSnapshot.getDiscountPolicy()).isInstanceOf(RateCouponDiscountPolicy.class);
        assertThat(cartCouponSnapshot.getDiscountPolicy().calculateDiscount(Money.wons(10000L))).isEqualTo(Money.wons(1000L));
        assertThat(cartCouponSnapshot.getDiscountPolicy().calculateDiscount(Money.wons(150000L))).isEqualTo(Money.wons(10000L));
    }
}