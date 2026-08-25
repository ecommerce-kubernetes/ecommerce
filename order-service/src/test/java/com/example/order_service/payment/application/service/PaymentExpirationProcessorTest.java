package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.application.service.fixture.PaymentResultFixture;
import com.example.order_service.payment.config.PaymentProperties;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentExpirationProcessorTest {
    @InjectMocks
    private PaymentExpirationProcessor expirationService;

    @Mock
    private PaymentProperties paymentProperties;

    @Mock
    private PaymentQueryService paymentQueryService;

    @Mock
    private PaymentCommandService paymentCommandService;

    @Test
    @DisplayName("타임아웃 대상 준비 결제가 존재하면, 해당 결제들을 실패 처리한다.")
    void processTimeoutReadyPayments_whenExistTimeoutReadyPayments_thenChangeAbort() {
        // given
        LocalDateTime currentTime = LocalDateTime.now();
        given(paymentProperties.timeoutReady()).willReturn(30);

        PaymentResult payment1 = PaymentResultFixture.anPaymentResult()
                .paymentId(1L)
                .status(PaymentStatus.READY).build();
        PaymentResult payment2 = PaymentResultFixture.anPaymentResult()
                .paymentId(2L)
                .status(PaymentStatus.READY).build();

        given(paymentQueryService.getPaymentsByReadyAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of(payment1, payment2));

        PaymentFailure paymentFailure = PaymentFailure.of("TIMEOUT", "결제 준비 타임 아웃");
        // when
        expirationService.processTimeoutReadyPayments(currentTime);
        // then
        verify(paymentCommandService, times(1)).abort(payment1.paymentId(), paymentFailure);
        verify(paymentCommandService, times(1)).abort(payment2.paymentId(), paymentFailure);
    }

    @Test
    @DisplayName("타임아웃 대상 주문이 없으면 스킵된다")
    void processTimeoutReadyPayments_whenNotExistTimeoutOrders_thenSkip() {
        // given
        LocalDateTime currentTime = LocalDateTime.now();
        given(paymentProperties.timeoutReady()).willReturn(30);
        given(paymentQueryService.getPaymentsByReadyAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());

        // when
        expirationService.processTimeoutReadyPayments(currentTime);

        // then
        verify(paymentCommandService, never()).abort(anyLong(), any());
    }

    @Test
    @DisplayName("특정 주문 처리 중 예외가 발생하더라도, 다음 주문은 정상적으로 처리되어야 한다.")
    void processTimeoutOrders_whenThrownExceptionOrder_thenProcessingOtherOrders() {
        // given
        LocalDateTime currentTime = LocalDateTime.now();
        given(paymentProperties.timeoutReady()).willReturn(30);

        PaymentResult payment1 = PaymentResultFixture.anPaymentResult()
                .paymentId(1L)
                .status(PaymentStatus.READY).build();
        PaymentResult payment2 = PaymentResultFixture.anPaymentResult()
                .paymentId(2L)
                .status(PaymentStatus.READY).build();
        given(paymentQueryService.getPaymentsByReadyAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of(payment1, payment2));

        PaymentFailure paymentFailure = PaymentFailure.of("TIMEOUT", "결제 준비 타임 아웃");
        willThrow(new RuntimeException("DB Connection Timeout"))
                .given(paymentCommandService).abort(1L, paymentFailure);

        // when
        expirationService.processTimeoutReadyPayments(currentTime);

        // then
        verify(paymentCommandService, times(1)).abort(1L, paymentFailure);
        verify(paymentCommandService, times(1)).abort(2L, paymentFailure);
    }
}