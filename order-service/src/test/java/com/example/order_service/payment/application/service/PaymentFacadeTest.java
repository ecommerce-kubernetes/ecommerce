package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        PaymentOrderResult orderResult = createPaymentOrderResult(PaymentOrderStatus.PENDING);

        PaymentResult paymentResult = PaymentResult.builder()
                .paymentId(paymentId)
                .orderId(1L)
                .userId(1L)
                .status(PaymentStatus.APPROVAL_PENDING)
                .totalAmount(Money.wons(10000L))
                .build();

        given(paymentOrderPort.getOrder(anyLong(), anyLong())).willReturn(orderResult);
        given(paymentCommandService.create(any(CreatePaymentContext.class))).willReturn(paymentId);
        given(paymentQueryService.getPayment(anyLong(), anyLong())).willReturn(paymentResult);
        //when
        PaymentCreateResult result = paymentFacade.create(command);
        //then
        assertThat(result.paymentId()).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentStatus.APPROVAL_PENDING);
        assertThat(result.orderId()).isEqualTo(1L);
        assertThat(result.totalAmount()).isEqualTo(Money.wons(10000L));
    }

    @Test
    @DisplayName("주문이 결제 대기 상태가 아니라면 예외가 발생한다.")
    void create_orderStatus_not_pending() {
        //given
        Long paymentId = 1L;
        PaymentCreateCommand command = PaymentCreateCommand.builder()
                .userId(1L)
                .orderId(1L)
                .build();

        PaymentOrderResult order = createPaymentOrderResult(PaymentOrderStatus.CANCELED);

        given(paymentOrderPort.getOrder(anyLong(), anyLong())).willReturn(order);
        //when
        //then
        assertThatThrownBy(() -> paymentFacade.create(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.ORDER_NOT_PENDING);
    }

    private PaymentOrderResult createPaymentOrderResult(PaymentOrderStatus status) {
        return PaymentOrderResult.builder()
                .orderId(1L)
                .status(status)
                .orderName("상품")
                .totalAmount(Money.wons(10000L))
                .build();
    }
}