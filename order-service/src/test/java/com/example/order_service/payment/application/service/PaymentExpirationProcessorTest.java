package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGInquiryResult;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.application.service.fixture.PaymentPGResultFixture;
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
import static org.mockito.BDDMockito.*;
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

    @Mock
    private PaymentPGPort pgPort;

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

    @Test
    @DisplayName("타임아웃 대상 승인 대기 결제가 존재하지 않으면 스킵한다.")
    void processTimeoutApprovePendingPayments_whenNotExistTimeoutApprovePendingPayments_thenSkip() {
        // given
        LocalDateTime currentTime = LocalDateTime.now();
        given(paymentProperties.timeoutApprovePending()).willReturn(5);

        given(paymentQueryService.getPaymentsByApprovePendingAndUpdatedAtBefore(any(LocalDateTime.class)))
                .willReturn(Collections.emptyList());

        // when
        expirationService.processTimeoutApprovePendingPayments(currentTime);
        // then
        verify(pgPort, never()).inquiry(anyString(), any());
    }

    @Test
    @DisplayName("결제 승인대기 처리 중 PG 결제 상태가 완료이면 망취소를 진행하고 결제를 실패한다.")
    void processTimeoutApprovePendingPayments_whenPGStatusIsDone_thenNetCancelAndAbortPayment() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(paymentProperties.timeoutApprovePending()).willReturn(5);

        PaymentResult payment1 = PaymentResultFixture.anPaymentResult()
                .paymentId(1L)
                .status(PaymentStatus.APPROVAL_PENDING).build();

        PGInquiryResult paymentResult = PaymentPGResultFixture.anPGInquiryResult()
                .status(PaymentPGStatus.DONE).build();
        given(paymentQueryService.getPaymentsByApprovePendingAndUpdatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of(payment1));

        given(pgPort.inquiry(anyString(), any())).willReturn(paymentResult);
        willDoNothing().given(pgPort).netCancel(anyString(), anyString(), any());
        //when
        expirationService.processTimeoutApprovePendingPayments(currentTime);
        //then
        verify(pgPort).netCancel(anyString(), anyString(), any());
        verify(paymentCommandService).abort(anyLong(), any());
    }

    @Test
    @DisplayName("결제 승인대기 처리 중 PG 결제 상태가 실패이면 결제를 실패한다.")
    void processTimeoutApprovePendingPayments_whenPGStatusIsAbort_thenAbortPayment() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(paymentProperties.timeoutApprovePending()).willReturn(5);

        PaymentResult payment1 = PaymentResultFixture.anPaymentResult()
                .paymentId(1L)
                .status(PaymentStatus.APPROVAL_PENDING).build();

        PGInquiryResult paymentResult = PaymentPGResultFixture.anPGInquiryResult()
                .status(PaymentPGStatus.ABORTED)
                .failure(PGInquiryResult.PGFailureResult.builder()
                        .code("잔액부족")
                        .message("잔액이 부족합니다.")
                        .build())
                .build();
        given(paymentQueryService.getPaymentsByApprovePendingAndUpdatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of(payment1));

        given(pgPort.inquiry(anyString(), any())).willReturn(paymentResult);
        //when
        expirationService.processTimeoutApprovePendingPayments(currentTime);
        //then
        verify(paymentCommandService).abort(anyLong(), any());
    }

    @Test
    @DisplayName("결제 승인대기 처리 중 PG 결제 상태가 취소이면 결제를 실패한다.")
    void processTimeoutApprovePendingPayments_whenPGStatusIsCanceled_thenAbortPayment() {
        //given
        LocalDateTime currentTime = LocalDateTime.now();
        given(paymentProperties.timeoutApprovePending()).willReturn(5);

        PaymentResult payment1 = PaymentResultFixture.anPaymentResult()
                .paymentId(1L)
                .status(PaymentStatus.APPROVAL_PENDING).build();

        PGInquiryResult paymentResult = PaymentPGResultFixture.anPGInquiryResult()
                .status(PaymentPGStatus.CANCELED)
                .cancelReason("결제 타임아웃으로 인한 취소")
                .build();

        given(paymentQueryService.getPaymentsByApprovePendingAndUpdatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of(payment1));

        given(pgPort.inquiry(anyString(), any())).willReturn(paymentResult);
        //when
        expirationService.processTimeoutApprovePendingPayments(currentTime);
        //then
        verify(paymentCommandService).abort(anyLong(), any());
    }
}