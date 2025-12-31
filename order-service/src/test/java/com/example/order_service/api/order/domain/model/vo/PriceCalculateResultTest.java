package com.example.order_service.api.order.domain.model.vo;

import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PriceCalculateResultTest {

    @Test
    @DisplayName("쿠폰이 있는 경우 쿠폰 정보와 함께 매핑해 생성한다")
    void of_WithCoupon(){
        //given
        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );

        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);

        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        //when
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, coupon, 1000L, 28600L);
        //then
        assertThat(priceCalculateResult.getAppliedCoupon()).isNotNull();
        assertThat(priceCalculateResult.getAppliedCoupon())
                .extracting("couponId", "couponName", "discountAmount")
                        .containsExactly(1L, "1000원 할인 쿠폰", 1000L);
        assertThat(priceCalculateResult.getOrderPriceInfo())
                .extracting("totalOriginPrice", "totalProductDiscount", "couponDiscount", "usedPoint", "finalPaymentAmount")
                .containsExactly(34000L, 3400L, 1000L, 1000L, 28600L);
    }

    @Test
    @DisplayName("쿠폰이 없는 경우 쿠폰 할인 가격은 0원이고 적용된 쿠폰은 null이다")
    void of_WithoutCoupon(){
        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );

        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);
        //when
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, null, 1000L, 29600L);
        //then
        assertThat(priceCalculateResult.getAppliedCoupon()).isNull();
        assertThat(priceCalculateResult.getOrderPriceInfo())
                .extracting("totalOriginPrice", "totalProductDiscount", "couponDiscount", "usedPoint", "finalPaymentAmount")
                .containsExactly(34000L, 3400L, 0L, 1000L, 29600L);
    }

    @Test
    @DisplayName("예상 결제 금액과 최종 결제금액이 서로 다른 경우 예외를 던진다")
    void of_finalPaymentAmount_notMatch_expectedPrice(){
        //given
        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );

        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);

        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        //when
        //then
        assertThatThrownBy(() -> PriceCalculateResult.of(itemCalculationResult, coupon, 1000L, 30000L))
                .isInstanceOf(OrderVerificationException.class)
                .hasMessage("주문 금액이 변동되었습니다");
    }
}
