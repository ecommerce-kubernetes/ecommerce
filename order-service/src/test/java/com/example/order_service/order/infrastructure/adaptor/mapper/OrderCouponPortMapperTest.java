package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.coupon.CartCouponResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.order.application.port.dto.result.CartCouponResult;
import com.example.order_service.order.application.port.dto.result.ItemCouponResult;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.domain.policy.RateCouponDiscountPolicy;
import com.example.order_service.order.domain.vo.CartCouponSnapshot;
import com.example.order_service.order.domain.vo.ItemCouponSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCouponPortMapperTest {

    private OrderCouponPortMapper orderCouponPortMapper = new OrderCouponPortMapper();

    @Test
    @DisplayName("상품 쿠폰 조회 결과를 매핑한다 (정액 쿠폰)")
    void mapToItemCouponResult_fixed(){
        //given
        ItemCouponResponse response = ItemCouponResponse.builder()
                .userId(1L)
                .itemCouponId(1L)
                .name("1000원 할인 쿠폰")
                .applyQuantityLimit(3)
                .discountType("FIXED")
                .discountAmount(1000L)
                .build();
        //when
        ItemCouponResult result = orderCouponPortMapper.mapToItemCouponResult(response);
        //then
        ItemCouponSnapshot itemCouponSnapshot = result.itemCoupon();
        assertThat(itemCouponSnapshot)
                .extracting("itemCouponId", "name", "applyQuantityLimit")
                .containsExactly(1L, "1000원 할인 쿠폰", 3);

        assertThat(itemCouponSnapshot.getDiscountPolicy()).isInstanceOf(FixedCouponDiscountPolicy.class);
        assertThat(itemCouponSnapshot.getDiscountPolicy().calculateDiscount(Money.wons(10000L))).isEqualTo(Money.wons(1000L));
    }

    @Test
    @DisplayName("상품 쿠폰 조회 결과를 매핑한다 (정률 쿠폰)")
    void mapToItemCouponResult_rate(){
        //given
        ItemCouponResponse response = ItemCouponResponse.builder()
                .userId(1L)
                .itemCouponId(1L)
                .name("10% 할인 쿠폰")
                .applyQuantityLimit(1)
                .discountType("RATE")
                .discountRate(10)
                .maxDiscountAmount(10000L)
                .build();
        //when
        ItemCouponResult result = orderCouponPortMapper.mapToItemCouponResult(response);
        //then
        ItemCouponSnapshot itemCouponSnapshot = result.itemCoupon();
        assertThat(itemCouponSnapshot)
                .extracting("itemCouponId", "name", "applyQuantityLimit")
                .containsExactly(1L, "10% 할인 쿠폰", 1);

        assertThat(itemCouponSnapshot.getDiscountPolicy()).isInstanceOf(RateCouponDiscountPolicy.class);
        assertThat(itemCouponSnapshot.getDiscountPolicy().calculateDiscount(Money.wons(10000L))).isEqualTo(Money.wons(1000L));
        assertThat(itemCouponSnapshot.getDiscountPolicy().calculateDiscount(Money.wons(150000L))).isEqualTo(Money.wons(10000L));
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