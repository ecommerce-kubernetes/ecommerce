package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.*;
import com.example.order_service.payment.domain.repository.PaymentRepository;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.support.annotation.MockKafka;
import com.example.order_service.support.annotation.MockRedis;
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

    @Nested
    @DisplayName("결제 저장")
    class Save {

        @Test
        @DisplayName("결제를 저장한다")
        void save() {
            //given
            PaymentContext.Create context = PaymentContext.Create.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .totalAmount(Money.wons(10000L))
                    .build();
            //when
            PaymentResult.Default save = paymentCommandService.save(context);
            //then
            assertThat(save.id()).isNotNull();
            assertThat(save.orderNo()).isEqualTo("orderNo");
            assertThat(save.status()).isEqualTo(PaymentStatus.READY);
            assertThat(save.paymentKey()).isEqualTo("paymentKey");
            assertThat(save.totalAmount()).isEqualTo(Money.wons(10000L));
        }
    }

    @Nested
    @DisplayName("환불 대기 변경")
    class RefundPending {

        @Test
        @DisplayName("환불 대기 상태로 변경한다")
        void changeRefund() {
            //given
            String orderNo = "orderNo";
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
            payment.approval(approval, PaymentStatus.DONE);
            paymentRepository.save(payment);
            //when
            paymentCommandService.changeRefundPending(orderNo);
            //then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_PENDING);
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void changeRefund_payment_not_found() {
            //given
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.changeRefundPending("unknown"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("결제가 승인 완료 상태가 아니면 예외가 발생한다")
        void changeRefund_payment_status_not_done() {
            //given
            String orderNo = "orderNo";
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.changeRefundPending(orderNo))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class Cancel {

        @Test
        @DisplayName("결제 취소 레코드를 저장한다")
        void cancel() {
            //given
            String orderNo = "orderNo";
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
            payment.approval(approval, PaymentStatus.DONE);
            payment.refundPending();
            paymentRepository.save(payment);
            PaymentContext.Cancellation context = PaymentContext.Cancellation.builder()
                    .paymentId(payment.getId()).amount(Money.wons(10000L))
                    .status(PaymentStatus.CANCELED).method(PaymentMethod.CARD)
                    .approvedAt(LocalDateTime.now()).build();
            //when
            PaymentResult.PaymentCancel cancel = paymentCommandService.cancel(context);
            //then
            assertThat(cancel.status()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(cancel.method()).isEqualTo(PaymentMethod.CARD);
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void cancel_payment_notFound(){
            //given
            PaymentContext.Cancellation context = PaymentContext.Cancellation.builder()
                    .paymentId(999L).amount(Money.wons(10000L))
                    .status(PaymentStatus.CANCELED).method(PaymentMethod.CARD)
                    .approvedAt(LocalDateTime.now()).build();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.cancel(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("결제 상태가 환불 대기가 아니라면 예외가 발생한다")
        void cancel_payment_not_refund_pending(){
            //given
            String orderNo = "orderNo";
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
            payment.approval(approval, PaymentStatus.DONE);
            paymentRepository.save(payment);

            PaymentContext.Cancellation context = PaymentContext.Cancellation.builder()
                    .paymentId(payment.getId()).amount(Money.wons(10000L))
                    .status(PaymentStatus.CANCELED).method(PaymentMethod.CARD)
                    .approvedAt(LocalDateTime.now()).build();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.cancel(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS_FOR_REFUND_PENDING);
        }

        @Test
        @DisplayName("환불 가능 금액을 초과한 경우 예외가 발생한다")
        void cancel_payment_exceed_refundable_amount(){
            //given
            String orderNo = "orderNo";
            Payment payment = Payment.create(orderNo, 1L, "paymentKey", Money.wons(10000L));
            PaymentRecord approval = PaymentRecord.createApproval(Money.wons(10000L), PaymentMethod.CARD, LocalDateTime.now());
            payment.approval(approval, PaymentStatus.DONE);
            payment.refundPending();
            paymentRepository.save(payment);

            PaymentContext.Cancellation context = PaymentContext.Cancellation.builder()
                    .paymentId(payment.getId()).amount(Money.wons(11000L))
                    .status(PaymentStatus.CANCELED).method(PaymentMethod.CARD)
                    .approvedAt(LocalDateTime.now()).build();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.cancel(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.EXCEEDED_REFUNDABLE_AMOUNT);
        }
    }
}
