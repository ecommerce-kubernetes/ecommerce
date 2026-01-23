package com.example.order_service.api.order.infrastructure.client.coupon;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.support.ExcludeInfraTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureWireMock(port = 0)
public class OrderCouponClientCircuitBreakerTest extends ExcludeInfraTest {

    @Autowired
    private OrderCouponAdaptor orderCouponAdaptor;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp(){
        WireMock.reset();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("couponService");
        circuitBreaker.reset();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("resilience4j.circuitbreaker.configs.default.recordFailurePredicate",
                () -> "com.example.order_service.api.common.config.CircuitBreakerFailurePredicate");
        registry.add("resilience4j.circuitbreaker.configs.default.failureRateThreshold", () -> 50);
        registry.add("resilience4j.circuitbreaker.configs.default.slidingWindowSize", () -> 100);
        registry.add("resilience4j.circuitbreaker.instances.couponService.baseConfig", () -> "default");
        registry.add("resilience4j.circuitbreaker.instances.couponService.slidingWindowSize", () -> 10);
    }

    @Test
    @DisplayName("couponService 서킷 브레이커는 404에러가 여러번 발생해도 서킷브레이커가 닫혀있어야 한다")
    void circuitBreakerClosedWhen404(){
        //given
        stubFor(post(urlMatching("/internal/coupons/.*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                orderCouponAdaptor.calculateDiscount(1L, 1L, 3000L);
            } catch (Exception e) {}
        }
        //then
        verify(10, postRequestedFor(urlMatching("/internal/coupons/.*")));
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("couponService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("couponService 서킷 브레이커는 5xx 에러가 여러번 발생하면 서킷브레이커가 열려야 한다")
    void circuitBreakerOpenWhen5xx() {
        //given
        stubFor(post(urlMatching("/internal/coupons/.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                orderCouponAdaptor.calculateDiscount(1L, 1L, 3000L);
            } catch (Exception e) {}
        }
        //then
        verify(10, postRequestedFor(urlMatching("/internal/coupons/.*")));
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("couponService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThatThrownBy(() -> orderCouponAdaptor.calculateDiscount(1L, 1L, 3000L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.UNAVAILABLE);
    }
}
