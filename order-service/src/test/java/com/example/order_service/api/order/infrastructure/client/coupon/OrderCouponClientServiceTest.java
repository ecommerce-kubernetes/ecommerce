package com.example.order_service.api.order.infrastructure.client.coupon;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcRequest;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
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

public class OrderCouponClientServiceTest extends ExcludeInfraServiceTest {

    @Autowired
    private OrderCouponClientService orderCouponClientService;
    @MockitoBean
    private OrderCouponClient orderCouponClient;

    @Test
    @DisplayName("쿠폰서비스에서 할인 금액 정보를 조회한다")
    void calculateDiscount(){
        //given
        OrderCouponCalcResponse calcResponse = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .discountAmount(1000L)
                .finalPaymentAmount(3000L)
                .build();

        given(orderCouponClient.calculate(any(OrderCouponCalcRequest.class)))
                .willReturn(calcResponse);
        //when
        OrderCouponCalcResponse result = orderCouponClientService.calculateDiscount(1L, 1L, 3100L);
        //then
        assertThat(result)
                .extracting("couponId", "discountAmount", "finalPaymentAmount")
                .contains(1L, 1000L, 3000L);
    }

    @Test
    @DisplayName("서킷브레이커가 열렸을때 쿠폰 할인 정보를 조회하면 UnavailableService 예외를 던진다")
    void calculateDiscount_When_Open_CircuitBreaker(){
        //given
        willThrow(CallNotPermittedException.class)
                .given(orderCouponClient)
                .calculate(any(OrderCouponCalcRequest.class));
        //when
        //then
        assertThatThrownBy(() -> orderCouponClientService.calculateDiscount(1L, 1L, 3100L))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("쿠폰 서비스가 응답하지 않습니다");
    }

    @Test
    @DisplayName("쿠폰 할인 정보를 조회할때 쿠폰을 찾을 수 없는 경우 받은 예외를 그대로 던진다")
    void calculateDiscount_When_NotFound_Exception(){
        //given
        willThrow(new NotFoundException("쿠폰을 찾을 수 없습니다"))
                .given(orderCouponClient)
                .calculate(any(OrderCouponCalcRequest.class));
        //when
        //then
        assertThatThrownBy(() -> orderCouponClientService.calculateDiscount(1L, 1L, 3100L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("쿠폰 할인 정보를 조회할때 알 수 없는 에러가 발생한 경우 InternalServerError를 던진다")
    void calculateDiscount_When_InternalServerError(){
        //given
        willThrow(new RuntimeException("쿠폰 서비스 오류 발생"))
                .given(orderCouponClient)
                .calculate(any(OrderCouponCalcRequest.class));
        //when
        //then
        assertThatThrownBy(() -> orderCouponClientService.calculateDiscount(1L, 1L, 3100L))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("쿠폰 서비스에서 오류가 발생했습니다");
    }
}
