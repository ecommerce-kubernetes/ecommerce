package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.support.TestFixtureUtil;
import com.example.order_service.support.annotation.IsolatedTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@IsolatedTest
public class CouponAdaptorTest {
    @Autowired
    private CouponAdaptor couponAdaptor;
    @MockitoBean
    private CouponFeignClient client;

    @Test
    @DisplayName("쿠폰 서비스에 쿠폰 할인 정보를 조회한다")
    void calculate(){
        //given
        Long userId = 1L;
        Long couponId = 1L;
        Long totalPrice = 10000L;
        CouponClientResponse.Calculate mockResponse = TestFixtureUtil.giveMeOne(CouponClientResponse.Calculate.class);
        given(client.calculate(any(CouponClientRequest.Calculate.class)))
                .willReturn(mockResponse);
        //when
        CouponClientResponse.Calculate response = couponAdaptor.calculate(userId, couponId, totalPrice);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("쿠폰 서비스에 쿠폰 정보를 조회할때 서킷 브레이커가 열렸다면 시스템 예외로 변환하여 던진다")
    void calculate_circuitbreaker_open(){
        //given
        CallNotPermittedException circuitException = CallNotPermittedException
                .createCallNotPermittedException(CircuitBreaker.ofDefaults("test"));
        willThrow(circuitException).given(client)
                .calculate(any(CouponClientRequest.Calculate.class));
        //when
        //then
        assertThatThrownBy(() -> couponAdaptor.calculate(1L, 1L, 10000L))
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("쿠폰 서비스 서킷 브레이커 열림")
                .extracting("errorCode")
                .isEqualTo("CIRCUIT_BREAKER_OPEN");
    }

    @Test
    @DisplayName("쿠폰 서비스에서 쿠폰 정보를 조회할때 external System 예외가 던져지면 그대로 던진다")
    void calculate_external_system_exception(){
        //given
        willThrow(ExternalSystemException.class).given(client)
                .calculate(any(CouponClientRequest.Calculate.class));
        //when
        //then
        assertThatThrownBy(() -> couponAdaptor.calculate(1L, 1L, 10000L))
                .isInstanceOf(ExternalSystemException.class);
    }

    @Test
    @DisplayName("쿠폰 서비스에서 쿠폰 정보를 조회할 때 알 수 없는 예외(디코더에서 변환 x) 가 던져지면 시스템 예외로 변환하여 던진다")
    void calculate_other_exception(){
        //given
        willThrow(RuntimeException.class).given(client)
                .calculate(any(CouponClientRequest.Calculate.class));
        //when
        //then
        assertThatThrownBy(() -> couponAdaptor.calculate(1L, 1L, 10000L))
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("쿠폰 서비스 통신 장애");
    }
}
