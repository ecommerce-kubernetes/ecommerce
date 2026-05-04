package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.TossFeignClient;
import com.example.order_service.infrastructure.dto.request.TossClientRequest;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossAdaptor {
    private final TossFeignClient client;

    @CircuitBreaker(name = "tossPaymentService", fallbackMethod = "confirmPaymentFallback")
    public TossClientResponse.Confirm confirmPayment(String orderId, String paymentKey, Long amount) {
        TossClientRequest.Confirm request = TossClientRequest.Confirm.of(orderId, paymentKey, amount);
        return client.confirmPayment(request);
    }

    @CircuitBreaker(name = "tossPaymentService", fallbackMethod = "cancelPaymentFallback")
    public TossClientResponse.Cancel cancelPayment(String paymentKey, String cancelReason, Long cancelAmount) {
        TossClientRequest.Cancel request = TossClientRequest.Cancel.of(cancelReason, cancelAmount);
        return client.cancelPayment(paymentKey, request);
    }

    private TossClientResponse.Confirm confirmPaymentFallback(String orderId, String paymentKey, Long amount, Throwable throwable) throws Throwable {
        if (throwable instanceof CallNotPermittedException) {
            log.error("토스 페이먼츠 장애로 인해 서킷 브레이커 열림");
            throw new ExternalSystemUnavailableException("CIRCUIT_BREAKER_OPEN", "토스 페이먼츠 서킷 브레이커 열림", throwable);
        }

        if (throwable instanceof ExternalSystemException) {
            throw throwable;
        }

        throw new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "토스 페이먼츠 통신 장애", throwable);
    }

    private TossClientResponse.Cancel cancelPaymentFallback(String paymentKey, String cancelReason, Long cancelAmount, Throwable throwable) throws Throwable {
        if (throwable instanceof CallNotPermittedException) {
            log.error("토스 페이먼츠 장애로 인해 서킷 브레이커 열림");
            throw new ExternalSystemUnavailableException("CIRCUIT_BREAKER_OPEN", "토스 페이먼츠 서킷 브레이커 열림", throwable);
        }

        if (throwable instanceof ExternalSystemException) {
            throw throwable;
        }

        throw new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "토스 페이먼츠 통신 장애", throwable);
    }
}
