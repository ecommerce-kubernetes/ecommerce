package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.*;
import com.example.order_service.payment.domain.repository.PaymentRepository;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;

@SpringBootTest
@MockKafka
@MockRedis
@Transactional
@RecordApplicationEvents
public class PaymentCommandServiceTest {

    @Autowired
    private PaymentCommandService paymentCommandService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    @DisplayName("결제를 저장한다")
    void create() {
        //given
        PaymentContext.Create context = PaymentContext.Create.builder()
                .userId(1L)
                .orderNo("orderNo")
                .paymentKey("paymentKey")
                .totalAmount(Money.wons(10000L))
                .build();
        //when
        PaymentResult.Default save = paymentCommandService.create(context);
        //then
        assertThat(save.id()).isNotNull();
        assertThat(save.orderNo()).isEqualTo("orderNo");
        assertThat(save.status()).isEqualTo(PaymentStatus.READY);
        assertThat(save.paymentKey()).isEqualTo("paymentKey");
        assertThat(save.totalAmount()).isEqualTo(Money.wons(10000L));
    }

    @Nested
    @DisplayName("결제 승인")
    class Approve {

        @Test
        @DisplayName("결제 승인 처리 후 결제 완료 이벤트를 발행한다")
        void approve() {
            //given
            Payment payment = Payment.create("orderNo", 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            PaymentContext.Approval context = PaymentContext.Approval.builder()
                    .paymentId(payment.getId())
                    .status(PaymentStatus.DONE)
                    .amount(Money.wons(10000L))
                    .method(PaymentMethod.CARD)
                    .transactionKey("transactionKey")
                    .approvedAt(LocalDateTime.now())
                    .build();
            //when
            PaymentResult.PaymentApproval approve = paymentCommandService.approve(context);
            //then
            assertThat(approve)
                    .extracting("paymentKey", "orderNo", "totalAmount", "method", "status")
                    .containsExactlyInAnyOrder(
                            "paymentKey", "orderNo", Money.wons(10000L), PaymentMethod.CARD, PaymentStatus.DONE
                    );
            assertThat(payment)
                    .extracting("lastTransactionKey", "method", "status")
                    .containsExactly(
                            "transactionKey", PaymentMethod.CARD, PaymentStatus.DONE
                    );
            assertThat(payment.getPaymentRecords()).hasSize(1);
            long eventCount = applicationEvents.stream(PaymentCompleteEvent.class).count();
            assertThat(eventCount).isEqualTo(1);
            PaymentCompleteEvent event = applicationEvents.stream(PaymentCompleteEvent.class).findFirst().orElseThrow();
            assertThat(event.getOrderNo()).isEqualTo("orderNo");
            assertThat(event.getPaymentId()).isEqualTo(payment.getId());
        }

        @Test
        @DisplayName("결제 승인 금액이 결제 금액과 일치하지 않으면 예외가 발생한다")
        void approve_mismatch_approval_amount() {
            //given
            Payment payment = Payment.create("orderNo", 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            PaymentContext.Approval context = PaymentContext.Approval.builder()
                    .paymentId(payment.getId())
                    .status(PaymentStatus.DONE)
                    .amount(Money.wons(5000L))
                    .method(PaymentMethod.CARD)
                    .transactionKey("transactionKey")
                    .approvedAt(LocalDateTime.now())
                    .build();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.approve(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PG_APPROVAL_AMOUNT_MISMATCH);
        }

        @Test
        @DisplayName("현재 결제가 결제 승인을 할 수 없는 상태이면 예외가 발생한다")
        void approve_invalid_payment_status() {
            //given
            Payment payment = Payment.create("orderNo", 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord paymentRecord = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
            payment.approve(paymentRecord, PaymentStatus.DONE, PaymentMethod.CARD);
            paymentRepository.save(payment);
            PaymentContext.Approval context = Instancio.of(PaymentContext.Approval.class)
                    .set(field("paymentId"), payment.getId())
                    .create();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.approve(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_APPROVAL);
        }

        @Test
        @DisplayName("지원하지 않는 결제 방식인 경우 예외가 발생한다")
        void approve_unsupported_payment_status() {
            //given
            Payment payment = Payment.create("orderNo", 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            PaymentContext.Approval context = Instancio.of(PaymentContext.Approval.class)
                    .set(field("paymentId"), payment.getId())
                    .set(field("status"), PaymentStatus.WAITING_FOR_DEPOSIT)
                    .set(field("method"), PaymentMethod.VIRTUAL_ACCOUNT)
                    .create();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.approve(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.UNSUPPORTED_PAYMENT_METHOD);
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void approve_payment_not_found() {
            //given
            PaymentContext.Approval context = Instancio.create(PaymentContext.Approval.class);
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.approve(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class Abort {

        @Test
        @DisplayName("결제를 취소 처리한다")
        void abort(){
            //given
            Payment payment = Payment.create("orderNo", 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            //when
            paymentCommandService.abort(payment.getId(), "REJECT_ACCOUNT_PAYMENT");
            //then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
            assertThat(payment.getFailureCode()).isEqualTo("REJECT_ACCOUNT_PAYMENT");

        }

        @Test
        @DisplayName("결제 승인 실패시 결제를 찾을 수 없으면 예외가 발생한다")
        void abort_payment_not_found() {
            //given
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.abort(999L, "INSUFFICIENT_BALANCE"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("결제 승인 실패시 결제가 취소 가능한 상태가 아니면 예외가 발생한다")
        void abort_payment_not_ready(){
            //given
            Payment payment = Payment.create("orderNo", 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
            payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
            paymentRepository.save(payment);
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.abort(payment.getId(), "REJECT_ACCOUNT_PAYMENT"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_FAIL);
        }
    }

    @Nested
    @DisplayName("환불 대기 변경")
    class RefundPending {

        @Test
        @DisplayName("결제를 환불 대기로 변경한다")
        void changeRefund(){
            //given
            String orderNo = "orderNo";
            LocalDateTime refundPendingAt = LocalDateTime.now();
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
            payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
            paymentRepository.save(payment);
            //when
            paymentCommandService.changeRefundPending(payment.getId(), refundPendingAt);
            //then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
            assertThat(payment.getRefundPendingAt()).isEqualTo(refundPendingAt);
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void changeRefund_payment_not_found() {
            //given
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.changeRefundPending(999L, LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("결제가 승인 완료 상태가 아니면 예외가 발생한다")
        void changeRefund_payment_status_not_done() {
            //given
            String orderNo = "orderNo";
            LocalDateTime refundPendingAt = LocalDateTime.now();
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.changeRefundPending(payment.getId(), refundPendingAt))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING);
        }
    }

    @Nested
    @DisplayName("결제 환불")
    class Cancel {

        @Test
        @DisplayName("결제를 환불한다")
        void cancel(){
            //given
            String orderNo = "orderNo";
            LocalDateTime canceledAt = LocalDateTime.now();
            LocalDateTime refundPendingAt = LocalDateTime.now();
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
            payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
            payment.refundPending(refundPendingAt);
            paymentRepository.save(payment);

            PaymentContext.Cancellation context = PaymentContext.Cancellation.builder()
                    .paymentId(payment.getId())
                    .amount(Money.wons(10000L))
                    .status(PaymentStatus.CANCELED)
                    .transactionKey("lastTransactionKey")
                    .cancelReason("테스트 결제 취소")
                    .canceledAt(canceledAt)
                    .build();
            //when
            PaymentResult.PaymentCancel cancel = paymentCommandService.cancel(context);
            //then
            assertThat(cancel)
                    .extracting("orderNo", "canceledAmount", "status")
                    .containsExactly(
                            "orderNo", Money.wons(10000L), PaymentStatus.CANCELED
                    );

        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void cancel_not_found(){
            //given
            PaymentContext.Cancellation context = Instancio.create(PaymentContext.Cancellation.class);
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.cancel(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("결제가 환불 대기 상태가 아니면 예외가 발생한다")
        void cancel_payment_status_not_refundPending(){
            //given
            String orderNo = "orderNo";
            LocalDateTime canceledAt = LocalDateTime.now();
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
            payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
            paymentRepository.save(payment);
            PaymentContext.Cancellation context = PaymentContext.Cancellation.builder()
                    .paymentId(payment.getId())
                    .amount(Money.wons(10000L))
                    .status(PaymentStatus.CANCELED)
                    .transactionKey("lastTransactionKey")
                    .cancelReason("테스트 결제 취소")
                    .canceledAt(canceledAt)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.cancel(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND);
        }

        @Test
        @DisplayName("환불 가능한 금액을 초과한 경우 예외가 발생한다")
        void cancel_exceeded_refundable_amount(){
            //given
            String orderNo = "orderNo";
            LocalDateTime canceledAt = LocalDateTime.now();
            LocalDateTime refundPendingAt = LocalDateTime.now();
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
            payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
            payment.refundPending(refundPendingAt);
            paymentRepository.save(payment);

            PaymentContext.Cancellation context = PaymentContext.Cancellation.builder()
                    .paymentId(payment.getId())
                    .amount(Money.wons(11000L))
                    .status(PaymentStatus.CANCELED)
                    .transactionKey("lastTransactionKey")
                    .cancelReason("테스트 결제 취소")
                    .canceledAt(canceledAt)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.cancel(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.EXCEEDED_REFUNDABLE_AMOUNT);
        }
    }

    @Nested
    @DisplayName("결제 수동 확인 변경")
    class ManualCheck {

        @Test
        @DisplayName("결제 승인 수동 확인 상태로 변경한다")
        void changeApprovalManualCheck(){
            //given
            String orderNo = "orderNo";
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            //when
            paymentCommandService.changeApprovalManualCheck(payment.getId());
            //then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.MANUAL_CHECK);
            assertThat(payment.getManualCheckReason()).isEqualTo(PaymentManualCheckReason.APPROVAL_RECON);
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void changeApprovalManualCheck_notFound(){
            //given
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.changeApprovalManualCheck(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("결제 환불 수동 확인 상태로 변경한다")
        void changeRefundManualCheck(){
            //given
            String orderNo = "orderNo";
            LocalDateTime refundPendingAt = LocalDateTime.now();
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval("transactionKey", Money.wons(10000L), LocalDateTime.now());
            payment.approve(approval, PaymentStatus.DONE, PaymentMethod.CARD);
            payment.refundPending(refundPendingAt);
            paymentRepository.save(payment);
            //when
            paymentCommandService.changeApprovalManualCheck(payment.getId());
            //then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.MANUAL_CHECK);
            assertThat(payment.getManualCheckReason()).isEqualTo(PaymentManualCheckReason.REFUND_RECON);
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void changeRefundManualCheck_notFound(){
            //given
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.changeRefundManualCheck(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
    }
}
