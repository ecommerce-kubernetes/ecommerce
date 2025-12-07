package com.example.order_service.api.cart.infrastructure.client;

import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test-mock")
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@Slf4j
public class CartProductClientServiceCircuitBreakerTest {
    @MockitoBean
    private KafkaTemplate<String, Object> kafkaTemplate;
    @MockitoBean
    private RedisTemplate<String, Object> redisTemplate;
    @MockitoBean
    private RedisScript<Long> redisScript;

    @Autowired
    private CartProductClientService cartProductClientService;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("resilience4j.circuitbreaker.instances.productService.slidingWindowSize", () -> 10);
        registry.add("resilience4j.circuitbreaker.instances.productService.failureRateThreshold", () -> 50);
        registry.add("resilience4j.circuitbreaker.instances.productService.recordExceptions[0]",
                () -> "com.example.order_service.api.common.exception.server.InternalServerException");

        registry.add("resilience4j.circuitbreaker.instances.productService.ignoreExceptions[0]",
                () -> "com.example.order_service.api.common.exception.NotFoundException");
    }

    @Test
    @DisplayName("productService 서킷 브레이커는 404에러가 여러번 발생해도 서킷브레이커가 닫혀있어야 한다")
    void circuitBreakerClosedWhen404(){
        //given
        stubFor(get(urlMatching("/variants/.*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                cartProductClientService.getProduct(1L);
            } catch (Exception e) {}
        }
        //then

        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("productService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("productService 서킷 브레이커는 500 에러가 여러번 발생하면 서킷브레이커가 열려야 한다")
    void circuitBreakerOpenWhen5xx(){
        //given
        stubFor(get(urlMatching("/variants/.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                cartProductClientService.getProduct(1L);
            } catch (Exception e) {
            }
        }
        //then
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("productService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThatThrownBy(() -> cartProductClientService.getProduct(1L))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("상품 서비스가 응답하지 않습니다");
    }

}
