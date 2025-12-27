package com.example.order_service.api.order.infrastructure.client.payment;

import com.example.order_service.api.common.exception.server.UnavailableServiceException;
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
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@AutoConfigureWireMock(port = 0)
public class TossPaymentClientCircuitBreakerTest extends ExcludeInfraTest {
    @Autowired
    private TossPaymentClientService paymentClientService;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp(){
        WireMock.reset();

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("tossPaymentService");
        circuitBreaker.reset();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("resilience4j.circuitbreaker.instances.tossPaymentService.slidingWindowSize", () -> 10);
        registry.add("resilience4j.circuitbreaker.instances.tossPaymentService.failureRateThreshold", () -> 50);
        registry.add("resilience4j.circuitbreaker.instances.tossPaymentService.recordExceptions[0]",
                () -> "com.example.order_service.api.common.exception.server.InternalServerException");

        registry.add("resilience4j.circuitbreaker.instances.tossPaymentService.ignoreExceptions[0]",
                () -> "com.example.order_service.api.common.exception.PaymentException");

        registry.add("payment.toss.url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Test
    @DisplayName("couponService 서킷 브레이커는 404에러가 여러번 발생해도 서킷브레이커가 닫혀있어야 한다")
    void circuitBreakerClosedWhen404(){
        //given
        stubFor(post(urlMatching("/v1/payments/confirm"))
                .willReturn(aResponse()
                        .withStatus(403)
                        .withHeader("Content-Type", "application/json")));
        //when
        for (int i = 0; i < 10; i++) {
            try {
                paymentClientService.confirmPayment(1L, "paymentKey", 3000L);
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
                paymentClientService.confirmPayment(1L, "paymentKey", 3000L);
            } catch (Exception e) {}
        }
        //then
        verify(10, postRequestedFor(urlMatching("/v1/payments/confirm")));
        CircuitBreaker breaker = circuitBreakerRegistry.circuitBreaker("tossPaymentService");
        assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        assertThatThrownBy(() -> paymentClientService.confirmPayment(1L, "paymentKey", 3000L))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("토스 페이먼츠 서비스가 응답하지 않습니다");
    }
}
