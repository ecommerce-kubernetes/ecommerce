package com.example.order_service.payment.application.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.domain.model.PaymentMethod;
import com.example.order_service.payment.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMapperTest {
    private final PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    @DisplayName("결제 생성 컨텍스트 매핑")
    void toContext_create() {
        //given
        PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                .userId(1L)
                .orderNo("orderNo")
                .paymentKey("paymentKey")
                .amount(Money.wons(10000L))
                .build();
        PaymentContext.Create expected = PaymentContext.Create.builder()
                .userId(1L)
                .orderNo("orderNo")
                .paymentKey("paymentKey")
                .totalAmount(Money.wons(10000L))
                .build();
        //when
        PaymentContext.Create context = paymentMapper.toContext(command);
        //then
        assertThat(context).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("결제 승인 컨텍스트 매핑")
    void toContext_approval(){
        //given
        Long paymentId = 1L;
        LocalDateTime approvedAt = LocalDateTime.now();
        PgPaymentResult.Approval result = PgPaymentResult.Approval.builder()
                .status(PaymentStatus.DONE)
                .totalAmount(Money.wons(10000L))
                .method(PaymentMethod.CARD)
                .transactionKey("9C62B18EEF0DE3EB7F4422EB6D14BC6E")
                .approvedAt(approvedAt)
                .build();

        PaymentContext.Approval expected = PaymentContext.Approval.builder()
                .paymentId(paymentId)
                .status(PaymentStatus.DONE)
                .amount(Money.wons(10000L))
                .method(PaymentMethod.CARD)
                .transactionKey("9C62B18EEF0DE3EB7F4422EB6D14BC6E")
                .approvedAt(approvedAt)
                .build();
        //when
        PaymentContext.Approval approval = paymentMapper.toContext(paymentId, result);
        //then
        assertThat(approval).usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("결제 취소 컨텍스트 매핑")
    void toContext_cancellation() {
        //given
        Long paymentId = 1L;
        LocalDateTime canceledAt = LocalDateTime.now();
        PgPaymentResult.CancelReceipt cancelReceipt = PgPaymentResult.CancelReceipt.builder()
                .transactionKey("090A796806E726BBB929F4A2CA7DB9A7")
                .cancelAmount(Money.wons(10000L))
                .cancelReason("테스트 결제 취소")
                .canceledAt(canceledAt)
                .build();
        PaymentContext.Cancellation expected = PaymentContext.Cancellation.builder()
                .paymentId(paymentId)
                .amount(Money.wons(10000L))
                .status(PaymentStatus.CANCELED)
                .transactionKey("090A796806E726BBB929F4A2CA7DB9A7")
                .cancelReason("테스트 결제 취소")
                .canceledAt(canceledAt)
                .build();
        //when
        PaymentContext.Cancellation context = paymentMapper.toContext(paymentId, PaymentStatus.CANCELED, cancelReceipt);
        //then
        assertThat(context)
                .usingRecursiveComparison()
                .isEqualTo(expected);

    }
}