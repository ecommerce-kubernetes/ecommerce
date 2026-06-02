package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.payment.application.event.PaymentCompleteEvent;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.model.Payment;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.payment.domain.model.TransactionType;
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
    @DisplayName("결제 승인 레코드 저장")
    class Approve {

        @Test
        @DisplayName("결제 승인 레코드를 저장한다")
        void approve() {
            //given
            LocalDateTime approvedAt = LocalDateTime.now();
            Payment payment = Payment.create("orderNo", 1L, "paymentKey", Money.wons(10000L));
            paymentRepository.save(payment);
            PaymentContext.Approval context = PaymentContext.Approval.builder()
                    .paymentId(payment.getId())
                    .amount(Money.wons(10000L))
                    .status(PaymentStatus.DONE)
                    .method(PaymentMethod.CARD)
                    .approvedAt(approvedAt)
                    .build();
            //when
            PaymentResult.PaymentApproval approve = paymentCommandService.approve(context);
            //then
            assertThat(approve.status()).isEqualTo(PaymentStatus.DONE);
            assertThat(approve.method()).isEqualTo(PaymentMethod.CARD);

            assertThat(payment.getPaymentRecords()).hasSize(1);
            assertThat(payment.getPaymentRecords().get(0).getAmount()).isEqualTo(Money.wons(10000L));
            assertThat(payment.getPaymentRecords().get(0).getType()).isEqualTo(TransactionType.PAYMENT);

            long eventCount = applicationEvents.stream(PaymentCompleteEvent.class).count();
            assertThat(eventCount).isEqualTo(1);
            PaymentCompleteEvent paymentCompleteEvent = applicationEvents.stream(PaymentCompleteEvent.class).findFirst().orElseThrow();
            assertThat(paymentCompleteEvent.getOrderNo()).isEqualTo(payment.getOrderNo());
        }

        @Test
        @DisplayName("결제를 찾을 수 없으면 예외가 발생한다")
        void approve_not_found_payment() {
            //given
            LocalDateTime approvedAt = LocalDateTime.now();
            PaymentContext.Approval context = PaymentContext.Approval.builder()
                    .paymentId(999L)
                    .amount(Money.wons(10000L))
                    .status(PaymentStatus.DONE)
                    .method(PaymentMethod.CARD)
                    .approvedAt(approvedAt)
                    .build();
            //when
            //then
            assertThatThrownBy(() -> paymentCommandService.approve(context))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
    }
}
