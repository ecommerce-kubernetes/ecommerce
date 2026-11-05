package com.example.order_service.payment.application.service;

import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.PaymentStatus;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentReconcilerTest {

    @InjectMocks
    private PaymentReconciler paymentReconciler;

    @Mock
    private PaymentQueryService queryService;
    @Mock
    private PaymentCommandService commandService;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private PaymentMapper mapper;
    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-06-14T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Nested
    @DisplayName("승인 대사")
    class ReconcileReadyPayment {

        @Test
        @DisplayName("타임아웃된 결제가 PG 승인 실패인 경우 결제를 취소 처리한다")
        void reconcileReadyPayments_pg_abort() {
            //given
            PaymentResult.Default timeoutPayment = Instancio.of(PaymentResult.Default.class)
                    .set(field("status"), PaymentStatus.READY)
                    .create();
            PGPaymentResult.Inquiry inquire = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.ABORTED)
                    .create();
            given(queryService.getReadyPaymentsBefore(any(), anyInt()))
                    .willReturn(List.of(timeoutPayment));
            given(paymentGateway.inquire(any())).willReturn(inquire);
            //when
            paymentReconciler.reconcileReadyPayments();
            //then
            verify(commandService).abort(any(), any());
        }

        @Test
        @DisplayName("타임아웃된 결제가 없으면 아무런 동작도 하지 않는다")
        void reconcileReadyPayment_empty() {
            //given
            given(queryService.getReadyPaymentsBefore(any(), anyInt()))
                    .willReturn(Collections.emptyList());
            //when
            paymentReconciler.reconcileReadyPayments();
            //then
            verify(paymentGateway, never()).inquire(any());
        }

        @Test
        @DisplayName("타임아웃된 결제가 이미 PG 환불된 경우 결제를 취소 처리한다")
        void reconcileReadyPayments_pg_canceled() {
            //given
            PaymentResult.Default timeoutPayment = Instancio.of(PaymentResult.Default.class)
                    .set(field("status"), PaymentStatus.READY)
                    .create();
            PGPaymentResult.CancelReceipt receipt = Instancio.of(PGPaymentResult.CancelReceipt.class)
                    .set(field("cancelReason"), "PAYMENT_TIME_OUT")
                    .create();
            PGPaymentResult.Inquiry inquire = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.CANCELED)
                    .set(field("cancels"), List.of(receipt))
                    .create();
            given(queryService.getReadyPaymentsBefore(any(), anyInt()))
                    .willReturn(List.of(timeoutPayment));
            given(paymentGateway.inquire(any())).willReturn(inquire);
            //when
            paymentReconciler.reconcileReadyPayments();
            //then
            verify(commandService).abort(any(), any());
        }

        @Test
        @DisplayName("타임아웃된 결제가 PG 승인 성공인 경우 환불 하고 결제를 취소한다")
        void reconcileReadyPayments_pg_done() {
            //given
            PaymentResult.Default timeoutPayment = Instancio.of(PaymentResult.Default.class)
                    .set(field("status"), PaymentStatus.READY)
                    .create();
            PGPaymentResult.Inquiry inquire = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.DONE)
                    .create();
            PGPaymentResult.Cancellation cancellation = Instancio.of(PGPaymentResult.Cancellation.class)
                    .set(field("status"), PaymentStatus.CANCELED)
                    .create();
            given(queryService.getReadyPaymentsBefore(any(), anyInt()))
                    .willReturn(List.of(timeoutPayment));
            given(paymentGateway.inquire(any())).willReturn(inquire);
            given(paymentGateway.cancel(any())).willReturn(cancellation);
            //when
            paymentReconciler.reconcileReadyPayments();
            //then
            verify(commandService).abort(any(), any());
        }

        @Test
        @DisplayName("스로틀링 중 인터럽트가 발생하면 대사를 조기 종료한다")
        void reconcileReadyPayments_throttle_interrupt() {
            //given
            List<PaymentResult.Default> timeoutPayments = Instancio.ofList(PaymentResult.Default.class)
                    .size(2)
                    .set(field(PaymentResult.Default::status), PaymentStatus.READY)
                    .create();
            PGPaymentResult.Inquiry inquire = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.ABORTED)
                    .create();
            given(queryService.getReadyPaymentsBefore(any(), anyInt()))
                    .willReturn(timeoutPayments);
            given(paymentGateway.inquire(any())).willReturn(inquire);
            given(paymentGateway.inquire(any())).willAnswer(invocation -> {
                Thread.currentThread().interrupt();
                return inquire;
            });
            //when
            paymentReconciler.reconcileReadyPayments();
            //then
            verify(paymentGateway, times(1)).inquire(any());
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }

        @Test
        @DisplayName("결제 대사 처리중 예외가 발생해도 대사가 중단되지 않고 다음 결제건이 처리된다")
        void reconcileReady_continue_on_exception() {
            //given
            PaymentResult.Default payment1 = Instancio.of(PaymentResult.Default.class)
                    .set(field(PaymentResult.Default::status), PaymentStatus.READY)
                    .create();
            PaymentResult.Default payment2 = Instancio.of(PaymentResult.Default.class)
                    .set(field(PaymentResult.Default::status), PaymentStatus.READY)
                    .create();

            PGPaymentResult.Inquiry inquire2 = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.ABORTED)
                    .create();

            given(queryService.getReadyPaymentsBefore(any(), anyInt()))
                    .willReturn(List.of(payment1, payment2));

            given(paymentGateway.inquire(payment1.paymentKey()))
                    .willThrow(new RuntimeException("PG사 일시적 타임아웃"));
            given(paymentGateway.inquire(payment2.paymentKey()))
                    .willReturn(inquire2);
            //when
            paymentReconciler.reconcileReadyPayments();
            //then
            verify(commandService, times(1)).abort(eq(payment2.id()), any());
        }
    }

    @Nested
    @DisplayName("환불 대사")
    class ReconcileRefundPendingPayment {

        @Test
        @DisplayName("타임아웃된 결제가 PG 결제 완료 상태면 환불 후 결제를 환불 처리한다")
        void reconcileRefundPendingPayments_pg_complete() {
            //given
            PaymentResult.Default timeoutPayment = Instancio.of(PaymentResult.Default.class)
                    .set(field("status"), PaymentStatus.REFUND_PENDING)
                    .create();
            PGPaymentResult.Inquiry inquire = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.DONE)
                    .create();
            PGPaymentResult.Cancellation cancellation = Instancio.create(PGPaymentResult.Cancellation.class);
            given(queryService.getRefundPendingPaymentsBefore(any(), anyInt()))
                    .willReturn(List.of(timeoutPayment));
            given(paymentGateway.inquire(any())).willReturn(inquire);
            given(paymentGateway.cancel(any())).willReturn(cancellation);
            //when
            paymentReconciler.reconcileRefundPendingPayments();
            //then
            verify(commandService).cancel(any());
        }

        @Test
        @DisplayName("타임아웃된 결제가 없으면 아무런 동작도 하지 않는다")
        void reconcileRefundPendingPayment_empty() {
            //given
            given(queryService.getRefundPendingPaymentsBefore(any(), anyInt()))
                    .willReturn(Collections.emptyList());
            //when
            paymentReconciler.reconcileRefundPendingPayments();
            //then
            verify(paymentGateway, never()).inquire(any());
        }

        @Test
        @DisplayName("타임아웃된 결제가 이미 PG 환불된 경우 결제를 환불 처리한다")
        void reconcileRefundPendingPayments_pg_canceled() {
            //given
            PaymentResult.Default timeoutPayment = Instancio.of(PaymentResult.Default.class)
                    .set(field("status"), PaymentStatus.REFUND_PENDING)
                    .create();
            PGPaymentResult.CancelReceipt receipt = Instancio.of(PGPaymentResult.CancelReceipt.class)
                    .set(field("cancelReason"), "PAYMENT_TIME_OUT")
                    .create();
            PGPaymentResult.Inquiry inquire = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.CANCELED)
                    .set(field("cancels"), List.of(receipt))
                    .create();
            given(queryService.getRefundPendingPaymentsBefore(any(), anyInt()))
                    .willReturn(List.of(timeoutPayment));
            given(paymentGateway.inquire(any())).willReturn(inquire);
            //when
            paymentReconciler.reconcileRefundPendingPayments();
            //then
            verify(commandService).cancel(any());
        }

        @Test
        @DisplayName("스로틀링 중 인터럽트가 발생하면 대사를 조기 종료한다")
        void reconcileRefundPendingPayments_throttle_interrupt() {
            //given
            List<PaymentResult.Default> timeoutPayments = Instancio.ofList(PaymentResult.Default.class)
                    .size(2)
                    .set(field(PaymentResult.Default::status), PaymentStatus.REFUND_PENDING)
                    .create();
            PGPaymentResult.Inquiry inquire = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.CANCELED)
                    .create();
            given(queryService.getRefundPendingPaymentsBefore(any(), anyInt()))
                    .willReturn(timeoutPayments);
            given(paymentGateway.inquire(any())).willReturn(inquire);
            given(paymentGateway.inquire(any())).willAnswer(invocation -> {
                Thread.currentThread().interrupt();
                return inquire;
            });
            //when
            paymentReconciler.reconcileRefundPendingPayments();
            //then
            verify(paymentGateway, times(1)).inquire(any());
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        }

        @Test
        @DisplayName("결제 대사 처리중 예외가 발생해도 대사가 중단되지 않고 다음 결제건이 처리된다")
        void reconcileRefundPending_continue_on_exception() {
            //given
            PaymentResult.Default payment1 = Instancio.of(PaymentResult.Default.class)
                    .set(field(PaymentResult.Default::status), PaymentStatus.REFUND_PENDING)
                    .create();
            PaymentResult.Default payment2 = Instancio.of(PaymentResult.Default.class)
                    .set(field(PaymentResult.Default::status), PaymentStatus.REFUND_PENDING)
                    .create();

            PGPaymentResult.Inquiry inquire2 = Instancio.of(PGPaymentResult.Inquiry.class)
                    .set(field("status"), PaymentStatus.CANCELED)
                    .create();

            given(queryService.getRefundPendingPaymentsBefore(any(), anyInt()))
                    .willReturn(List.of(payment1, payment2));

            given(paymentGateway.inquire(payment1.paymentKey()))
                    .willThrow(new RuntimeException("PG사 일시적 타임아웃"));
            given(paymentGateway.inquire(payment2.paymentKey()))
                    .willReturn(inquire2);
            //when
            paymentReconciler.reconcileRefundPendingPayments();
            //then
            verify(commandService, times(1)).cancel(any());
        }
    }
}