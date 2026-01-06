package com.example.order_service.api.order.infrastructure.client.coupon;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcRequest;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.support.ExcludeInfraTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

public class OrderCouponClientServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderCouponClientService orderCouponClientService;
    @MockitoBean
    private OrderCouponClient orderCouponClient;

    @Test
    @DisplayName("쿠폰서비스에서 할인 금액 정보를 조회한다")
    void calculateDiscount(){
        //given
        OrderCouponDiscountResponse calcResponse = OrderCouponDiscountResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();

        given(orderCouponClient.calculate(any(OrderCouponCalcRequest.class)))
                .willReturn(calcResponse);
        //when
        OrderCouponDiscountResponse result = orderCouponClientService.calculateDiscount(1L, 1L, 3100L);
        //then
        assertThat(result)
                .extracting("couponId", "couponName", "discountAmount")
                .contains(1L, "1000원 할인 쿠폰", 1000L);
    }

    @Test
    @DisplayName("서킷브레이커가 열렸을때 쿠폰 할인 정보를 조회하면 예외를 던진다")
    void calculateDiscount_When_Open_CircuitBreaker(){
        //given
        willThrow(CallNotPermittedException.class)
                .given(orderCouponClient)
                .calculate(any(OrderCouponCalcRequest.class));
        //when
        //then
        assertThatThrownBy(() -> orderCouponClientService.calculateDiscount(1L, 1L, 3100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.UNAVAILABLE);
    }

    @Test
    @DisplayName("쿠폰 할인 정보를 조회할때 비지니스 예외가 발생한 경우 그대로 던진다")
    void calculateDiscount_When_BusinessException(){
        //given
        willThrow(BusinessException.class)
                .given(orderCouponClient)
                .calculate(any(OrderCouponCalcRequest.class));
        //when
        //then
        assertThatThrownBy(() -> orderCouponClientService.calculateDiscount(1L, 1L, 3100L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("쿠폰 할인 정보를 조회할때 알 수 없는 에러가 발생한 경우 서버 예외로 변환해 던진다")
    void calculateDiscount_When_InternalServerError(){
        //given
        willThrow(new RuntimeException("쿠폰 서비스 오류 발생"))
                .given(orderCouponClient)
                .calculate(any(OrderCouponCalcRequest.class));
        //when
        //then
        assertThatThrownBy(() -> orderCouponClientService.calculateDiscount(1L, 1L, 3100L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.SYSTEM_ERROR);
    }
}
