package com.example.order_service.api.order.infrastructure.client.payment;

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
public class TossPaymentClientCircuitBreakerTest extends ExcludeInfraTest {
    @Autowired
    private TossPaymentClientService paymentClientService;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @BeforeEach
    void setUp(){
        WireMock.reset();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("tossPaymentService");
        circuitBreaker.reset();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("resilience4j.circuitbreaker.configs.default.recordFailurePredicate",
                () -> "com.example.order_service.api.common.config.CircuitBreakerFailurePredicate");
        registry.add("resilience4j.circuitbreaker.configs.default.failureRateThreshold", () -> 50);
        registry.add("resilience4j.circuitbreaker.configs.default.slidingWindowSize", () -> 100);
        registry.add("resilience4j.circuitbreaker.instances.tossPaymentService.baseConfig", () -> "default");
        registry.add("resilience4j.circuitbreaker.instances.tossPaymentService.slidingWindowSize", () -> 10);

        registry.add("payment.toss.url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    @DisplayName("서킷 브레이커는 4xx 에러가 여러번 발생해도 서킷브레이커가 닫혀있어야 한다")
    void circuitBreakerClosedWhen404(){
        //given
        stubFor(post(urlMatching("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                paymentClientService.confirmPayment(ORDER_NO, "paymentKey", 3000L);
            } catch (Exception e) {}
        }
        //then
        verify(10,postRequestedFor(urlMatching("/v1/payments/confirm")));
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("tossPaymentService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    @DisplayName("서킷 브레이커는 5xx 에러가 여러번 발생하면 서킷브레이커가 열려야 한다")
    void circuitBreakerOpenWhen5xx() {
        //given
        stubFor(post(urlMatching("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                paymentClientService.confirmPayment(ORDER_NO, "paymentKey", 3000L);
            } catch (Exception e) {}
        }
        //then
        verify(10, postRequestedFor(urlMatching("/v1/payments/confirm")));
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("tossPaymentService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThatThrownBy(() -> paymentClientService.confirmPayment(ORDER_NO, "paymentKey", 3000L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.UNAVAILABLE);
    }
}
