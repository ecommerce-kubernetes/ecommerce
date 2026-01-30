package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;
import com.example.order_service.api.order.infrastructure.client.coupon.OrderCouponAdaptor;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.api.support.fixture.OrderCouponFixture.anOrderCouponDiscountResponse;
import static com.example.order_service.api.support.fixture.OrderCouponFixture.anOrderCouponInfo;
import static com.example.order_service.api.support.fixture.OrderPriceFixture.anOrderProductAmount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderCouponServiceTest {

    @InjectMocks
    private OrderCouponService orderCouponService;
    @Mock
    private OrderCouponAdaptor orderCouponAdaptor;

    @Nested
    @DisplayName("쿠폰 정보 조회")
    class GetCoupon {

        @Test
        @DisplayName("쿠폰 정보를 조회한다")
        void getCoupon(){
            //given
            OrderProductAmount productAmount = anOrderProductAmount().build();
            OrderCouponDiscountResponse coupon = anOrderCouponDiscountResponse().build();
            given(orderCouponAdaptor.calculateDiscount(anyLong(), anyLong(), anyLong()))
                    .willReturn(coupon);
            OrderCouponInfo expectedResult = anOrderCouponInfo().build();
            //when
            OrderCouponInfo result = orderCouponService.calculateCouponDiscount(1L, 1L, productAmount);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("쿠폰 아이디가 null 이면 할인 가격이 0원인 응답을 반환한다")
        void getCoupon_couponId_null(){
            //given
            OrderProductAmount productAmount = anOrderProductAmount().build();
            OrderCouponInfo expectedResult = anOrderCouponInfo().couponId(null).couponName(null).discountAmount(0L).build();
            //when
            OrderCouponInfo result = orderCouponService.calculateCouponDiscount(1L, null, productAmount);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult);
        }
    }
}
