package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentFacadeTest {

    @InjectMocks
    private PaymentFacade paymentFacade;

    @Mock
    private PaymentOrderPort paymentOrderPort;
    @Mock
    private PaymentPGPort paymentPGPort;
    @Mock
    private PaymentCommandService paymentCommandService;
    @Mock
    private PaymentQueryService paymentQueryService;
    @Spy
    private PaymentValidator validator;
    @Spy
    private PaymentContextFactory contextFactory;
    @Spy
    private PGErrorPolicy pgErrorPolicy;

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
    @DisplayName("결제를 승인한다.")
    void approve() {
        //given
        PaymentConfirmCommand command = PaymentConfirmCommand.builder()
                .paymentId(1L)
                .userId(1L)
                .paymentKey("paymentKey")
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .build();

        PGConfirmResult pgResult = PGConfirmResult.builder()
                .status(PaymentPGStatus.DONE)
                .amount(Money.wons(1000L))
                .method(PaymentMethod.CARD)
                .transactionKey("transactionKey")
                .approvedAt(LocalDateTime.now())
                .build();

        willDoNothing()
                .given(paymentCommandService)
                .approvePending(anyLong(), any(ApprovePendingPaymentContext.class));
        given(paymentPGPort.confirm(anyLong(), anyString(), any(), any()))
                .willReturn(pgResult);
        //when
        PaymentConfirmResult result = paymentFacade.approve(command);
        //then
        assertThat(result.paymentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("승인 금액이 결제 금액과 동일하지 않으면 결제를 실패 처리하고 예외가 발생한다.")
    void approve_payment_misMatch_amount() {
        //given
        PaymentConfirmCommand command = PaymentConfirmCommand.builder()
                .paymentId(1L)
                .userId(1L)
                .paymentKey("paymentKey")
                .amount(Money.wons(1000L))
                .provider(PaymentProvider.TOSS)
                .build();

        PaymentResult paymentResult = PaymentResult.builder()
                .paymentId(1L)
                .orderId(1L)
                .userId(1L)
                .status(PaymentStatus.READY)
                .totalAmount(Money.wons(1000L))
                .build();

        given(paymentQueryService.getPayment(anyLong(), anyLong())).willReturn(paymentResult);
        willThrow(new BusinessException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH))
                .given(paymentCommandService).approvePending(anyLong(), any(ApprovePendingPaymentContext.class));
        //when
        //then
        assertThatThrownBy(() -> paymentFacade.approve(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);

        verify(paymentCommandService).abort(anyLong(), any(PaymentFailure.class));
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