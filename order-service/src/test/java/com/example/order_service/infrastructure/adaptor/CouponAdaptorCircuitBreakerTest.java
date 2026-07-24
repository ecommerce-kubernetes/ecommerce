package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.support.annotation.IsolatedTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@IsolatedTest
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.couponService.sliding-window-size=3",
        "resilience4j.circuitbreaker.instances.couponService.minimum-number-of-calls=3",
        "resilience4j.circuitbreaker.instances.couponService.failure-rate-threshold=100",
        "resilience4j.circuitbreaker.instances.couponService.ignore-exceptions[0]=com.example.order_service.common.exception.external.ExternalClientException"
})
public class CouponAdaptorCircuitBreakerTest {

    @Autowired
    private CouponAdaptor adaptor;
    @MockitoBean
    private CouponFeignClient client;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("couponService").reset();
    }

    @Test
    @DisplayName("쿠폰 서비스에서 연속으로 서버 에러가 발생한 경우 서킷 브레이커가 열려 요청이 차단된다")
    void circuitbreaker_opens_after_consecutive_server_failures() {
        //given
        given(client.getItemCoupon(anyLong(), anyLong()))
                .willThrow(new RuntimeException("Connection Timeout"));
        //when
        //then
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> adaptor.getItemCoupon(1L, 1L))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("COUPON-SERVICE 통신 장애");
        }

        assertThatThrownBy(() -> adaptor.getItemCoupon(1L, 1L))
                .isInstanceOf(ExternalCircuitBreakerException.class)
                .hasMessage("COUPON-SERVICE 서킷 차단")
                .extracting("errorCode")
                .isEqualTo("CIRCUIT_BREAKER_OPEN");

        verify(client, times(3)).getItemCoupon(anyLong(), anyLong());
    }

    @Test
    @DisplayName("쿠폰 서비스에서 연속으로 클라이언트 에러가 발생한 경우 서킷 브레이커는 닫혀있어야 한다")
    void circuitbreaker_close_after_consecutive_client_failures() {
        //given
        given(client.getItemCoupon(anyLong(), anyLong()))
                .willThrow(new ExternalClientException("NOT_FOUND_COUPON", "쿠폰을 찾을 수 없습니다"));
        //when
        //then
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> adaptor.getItemCoupon(1L, 1L))
                    .isInstanceOf(ExternalClientException.class);
        }
        assertThatThrownBy(() -> adaptor.getItemCoupon(1L, 1L))
                .isInstanceOf(ExternalClientException.class);
        verify(client, times(4)).getItemCoupon(anyLong(), anyLong());
    }

}
