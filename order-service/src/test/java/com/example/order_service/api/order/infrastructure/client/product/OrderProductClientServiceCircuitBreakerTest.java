package com.example.order_service.api.order.infrastructure.client.product;

import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureWireMock(port = 0)
public class OrderProductClientServiceCircuitBreakerTest extends ExcludeInfraServiceTest {

    @Autowired
    private OrderProductClientService orderProductClientService;
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
        stubFor(post(urlMatching("/internal/variants/.*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                orderProductClientService.getProducts(List.of(1L, 2L));
            } catch (Exception e) {}
        }
        //then
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("productService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("productService 서킷 브레이커는 5xx에러가 여러번 발생하면 서킷브레이커가 열려야 한다")
    void circuitBreakerOpenWhen5xx() {
        //given
        stubFor(post(urlMatching("/internal/variants/.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                orderProductClientService.getProducts(List.of(1L, 2L));
            } catch (Exception e) {}
        }
        //then
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("productService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThatThrownBy(() -> orderProductClientService.getProducts(List.of(1L, 2L)))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("상품 서비스가 응답하지 않습니다");
    }
}
