package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.CouponFeignClient;
import com.example.order_service.infrastructure.dto.request.CouponClientRequest;
import com.example.order_service.support.annotation.IsolatedTest;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@IsolatedTest
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.couponService.sliding-window-size=3",
        "resilience4j.circuitbreaker.instances.couponService.minimum-number-of-calls=3",
        "resilience4j.circuitbreaker.instances.couponService.failure-rate-threshold=100",
        // 서킷 브레이커 카운트 제외
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
    void circuitbreaker_opens_after_consecutive_server_failures(){
        //given
        given(client.calculate(any(CouponClientRequest.Calculate.class)))
                .willThrow(new RuntimeException("Connection Timeout"));
        //when
        //then
        for (int i = 0; i <3; i++) {
            assertThatThrownBy(() -> adaptor.calculate(1L, 1L, 10000L))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("쿠폰 서비스 통신 장애");
        }

        assertThatThrownBy(() -> adaptor.calculate(1L, 1L, 10000L))
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("CircuitBreaker Open");

        //서킷 브레이커가 열렸으므로 클라이언트는 4번의 요청중 3번만 호출됨
        verify(client, times(3)).calculate(any(CouponClientRequest.Calculate.class));
    }

    @Test
    @DisplayName("쿠폰 서비스에서 연속으로 클라이언트 에러가 발생한 경우 서킷 브레이커는 닫혀있어야 한다")
    void circuitbreaker_close_after_consecutive_client_failures(){
        //given
        given(client.calculate(any(CouponClientRequest.Calculate.class)))
                .willThrow(new ExternalClientException("Client Exception"));
        //when
        //then
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> adaptor.calculate(1L, 1L, 10000L))
                    .isInstanceOf(ExternalClientException.class)
                    .hasMessage("Client Exception");
        }
        assertThatThrownBy(() -> adaptor.calculate(1L, 1L, 10000L))
                .isInstanceOf(ExternalClientException.class)
                .hasMessage("Client Exception");

        //반복된 에러가 클라이언트 예외이므로 정상 요청이 실행되어 4번 호출됨
        verify(client, times(4)).calculate(any());
    }

}
