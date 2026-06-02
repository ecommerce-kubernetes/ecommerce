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
        LocalDateTime approvedAt = LocalDateTime.now();
        PgPaymentResult.Approval result = PgPaymentResult.Approval.builder()
                .orderNo("orderNo")
                .paymentKey("paymentKey")
                .totalAmount(Money.wons(10000L))
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .approvedAt(approvedAt)
                .build();

        PaymentContext.Approval expected = PaymentContext.Approval.builder()
                .paymentId(1L)
                .amount(Money.wons(10000L))
                .status(PaymentStatus.DONE)
                .method(PaymentMethod.CARD)
                .approvedAt(approvedAt)
                .build();
        //when
        PaymentContext.Approval approval = paymentMapper.toContext(1L, result);
        //then
        assertThat(approval).usingRecursiveComparison()
                .isEqualTo(expected);
    }
}