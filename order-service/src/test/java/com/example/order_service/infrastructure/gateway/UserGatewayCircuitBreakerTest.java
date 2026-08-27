package com.example.order_service.infrastructure.gateway;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.UserFeignClient;
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
        "resilience4j.circuitbreaker.instances.userService.sliding-window-size=3",
        "resilience4j.circuitbreaker.instances.userService.minimum-number-of-calls=3",
        "resilience4j.circuitbreaker.instances.userService.failure-rate-threshold=100",
        "resilience4j.circuitbreaker.instances.userService.ignore-exceptions[0]=com.example.order_service.common.exception.external.ExternalClientException"
})
public class UserGatewayCircuitBreakerTest {

    @Autowired
    private UserGateway adaptor;
    @MockitoBean
    private UserFeignClient client;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry.circuitBreaker("userService").reset();
    }

    @Test
    @DisplayName("유저 서비스에서 연속으로 서버 에러가 발생한 경우 서킷 브레이커가 열려 요청이 차단된다")
    void circuitBreaker_opens_after_consecutive_server_failures(){
        //given
        Long userId = 1L;
        given(client.getUserProfile(anyLong()))
                .willThrow(new RuntimeException("Connection Timeout"));
        //when
        //then
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> adaptor.getUserProfile(userId))
                    .isInstanceOf(ExternalSystemUnavailableException.class)
                    .hasMessage("USER-SERVICE 통신 장애");
        }

        assertThatThrownBy(() -> adaptor.getUserProfile(userId))
                .isInstanceOf(ExternalCircuitBreakerException.class)
                .hasMessage("USER-SERVICE 서킷 차단")
                .extracting("errorCode")
                .isEqualTo("CIRCUIT_BREAKER_OPEN");

        verify(client, times(3)).getUserProfile(anyLong());
    }

    @Test
    @DisplayName("유저 서비스에서 연속으로 클라이언트 에러가 발생한 경우 서킷브레이커는 닫혀있어야 한다")
    void circuitbreaker_close_after_consecutive_client_failures(){
        //given
        Long userId = 1L;
        given(client.getUserProfile(anyLong()))
                .willThrow(new ExternalClientException("NOT_FOUND_USER", "유저를 찾을 수 없습니다"));
        //when
        //then
        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> adaptor.getUserProfile(userId))
                    .isInstanceOf(ExternalClientException.class);
        }
        assertThatThrownBy(() -> adaptor.getUserProfile(userId))
                .isInstanceOf(ExternalClientException.class);

        verify(client, times(4)).getUserProfile(anyLong());
    }
}
