package com.example.order_service.order.adapter.out.client.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponsResponse;
import com.example.order_service.order.application.port.dto.CartCouponResult;
import com.example.order_service.order.application.port.dto.ItemCouponsResult;
import com.example.order_service.order.application.port.dto.OrderCouponStatus;
import com.example.order_service.order.domain.ordersheet.CartCouponSnapshot;
import com.example.order_service.order.domain.ordersheet.ItemCouponSnapshot;
import com.example.order_service.order.domain.policy.CouponDiscountPolicy;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class OrderCouponAdapterMapperTest {

    private final OrderCouponAdaptorMapper orderCouponAdaptorMapper = new OrderCouponAdaptorMapper();

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
                .status("USED")
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
        ItemCouponsResult result = orderCouponAdaptorMapper.mapToItemCouponsResult(response);
        //then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.itemCoupons()).hasSize(2)
                .extracting("status", "itemCoupon", "expiresAt")
                .containsExactly(
                        tuple(OrderCouponStatus.AVAILABLE, expectedSnapshot1, now),
                        tuple(OrderCouponStatus.USED, expectedSnapshot2, now)
                );
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회 결과를 매핑한다 (정액 쿠폰)")
    void mapToCartCouponResult_fixed(){
        //given
        LocalDateTime expiresAt = LocalDateTime.now();
        CartCouponResponse response = CartCouponResponse.builder()
                .userId(1L)
                .cartCouponId(1L)
                .status("AVAILABLE")
                .name("장바구니 1000원 할인 쿠폰")
                .minimumPaymentAmount(10000L)
                .discountType("FIXED")
                .discountAmount(1000L)
                .expiresAt(expiresAt)
                .build();

        CouponDiscountPolicy discountPolicy = new FixedCouponDiscountPolicy(Money.wons(1000L));
        CartCouponSnapshot cartCouponSnapshot = CartCouponSnapshot.of(1L, "장바구니 1000원 할인 쿠폰", discountPolicy, Money.wons(10000L));
        //when
        CartCouponResult result = orderCouponAdaptorMapper.mapToCartCouponResult(response);
        //then
        assertThat(result.status()).isEqualTo(OrderCouponStatus.AVAILABLE);
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
        assertThat(result.cartCoupon()).isEqualTo(cartCouponSnapshot);
    }

    @Test
    @DisplayName("장바구니 쿠폰 조회 결과를 매핑한다 (정률 쿠폰)")
    void mapToCartCouponResult_rate(){
        //given
        LocalDateTime expiresAt = LocalDateTime.now();
        CartCouponResponse response = CartCouponResponse.builder()
                .userId(1L)
                .cartCouponId(1L)
                .status("USED")
                .name("장바구니 10% 할인 쿠폰")
                .minimumPaymentAmount(10000L)
                .discountType("RATE")
                .discountRate(10)
                .maxDiscountAmount(10000L)
                .expiresAt(expiresAt)
                .build();

        CouponDiscountPolicy discountPolicy = new RateCouponDiscountPolicy(10, Money.wons(10000L));
        CartCouponSnapshot cartCouponSnapshot = CartCouponSnapshot.of(1L, "장바구니 10% 할인 쿠폰", discountPolicy, Money.wons(10000L));
        //when
        CartCouponResult result = orderCouponAdaptorMapper.mapToCartCouponResult(response);
        //then
        assertThat(result.status()).isEqualTo(OrderCouponStatus.USED);
        assertThat(result.expiresAt()).isEqualTo(expiresAt);
        assertThat(result.cartCoupon()).isEqualTo(cartCouponSnapshot);
    }
}