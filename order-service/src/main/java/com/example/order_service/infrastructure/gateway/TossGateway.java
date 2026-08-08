package com.example.order_service.infrastructure.gateway;

import com.example.order_service.infrastructure.client.TossFeignClient;
import com.example.order_service.infrastructure.dto.request.TossClientRequest;
import com.example.order_service.infrastructure.dto.request.TossConfirmRequest;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TOSS PG와의 통신을 담당하는 Adaptor
 * <p>
 * TOSS PG FeignClient 호출, TOSS PG에 에러 발생시 서킷 브레이커를 통해 예외 전파를 관리
 * </p>
 *
 * @author 최민식
 * @since 2026. 06. 16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossGateway {
    private final TossFeignClient client;
    private final ExternalExceptionTranslator translator;

    @CircuitBreaker(name = "tossPaymentService", fallbackMethod = "confirmPaymentFallback")
    public TossConfirmResponse confirmPayment(Long orderId, String paymentKey, Long amount) {
        TossConfirmRequest request = TossConfirmRequest.of(orderId, paymentKey, amount);
        return client.confirmPayment(request);
    }

    private TossConfirmResponse confirmPaymentFallback(Long orderId, String paymentKey, Long amount, Throwable throwable) throws Throwable {
        throw translator.translate("TOSS-PAYMENTS", throwable);
    }

    @CircuitBreaker(name = "tossPaymentService", fallbackMethod = "cancelPaymentFallback")
    public TossClientResponse.Cancel cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        TossClientRequest.Cancel request = TossClientRequest.Cancel.of(cancelReason, cancelAmount);
        return client.cancelPayment(paymentKey, request);
    }

    private TossClientResponse.Cancel cancelPaymentFallback(String paymentKey, String cancelReason, Long cancelAmount, Throwable throwable) throws Throwable {
        throw translator.translate("TOSS-PAYMENTS", throwable);
    }

    @CircuitBreaker(name = "tossPaymentService", fallbackMethod = "inquirePaymentFallback")
    public TossClientResponse.Inquiry inquirePayment(String paymentKey) {
        return client.inquirePayment(paymentKey);
    }

    private TossClientResponse.Inquiry inquirePaymentFallback(String paymentKey, Throwable throwable) throws Throwable {
        throw translator.translate("TOSS-PAYMENTS", throwable);
    }
}
