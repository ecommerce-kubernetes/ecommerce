package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Mock
    private PaymentOrderPort paymentOrderPort;
    @Mock
    private PaymentCommandService paymentCommandService;
    @Mock
    private PaymentQueryService paymentQueryService;

    @Test
    @DisplayName("결제를 생성한다.")
    void create() {
        //given
        Long paymentId = 1L;
        PaymentCreateCommand command = PaymentCreateCommand.builder()
                .userId(1L)
                .orderId(1L)
                .build();

        PaymentOrderResult orderResult = createPaymentOrderResult();

        given(paymentOrderPort.getOrder(anyLong(), anyLong())).willReturn(orderResult);
        given(paymentCommandService.create(any(CreatePaymentContext.class))).willReturn(paymentId);
        //when
        PaymentCreateResult result = paymentFacade.create(command);
        //then
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.APPROVAL_PENDING);
    }

    private PaymentOrderResult createPaymentOrderResult() {
        return PaymentOrderResult.builder()
                .orderId(1L)
                .status(PaymentOrderStatus.PENDING)
                .orderName("상품")
                .totalAmount(Money.wons(10000L))
                .build();
    }
}