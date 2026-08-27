package com.example.order_service.payment.application.service;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.payment.application.port.PaymentOrderPort;
import com.example.order_service.payment.application.port.PaymentPGPort;
import com.example.order_service.payment.application.port.dto.PGCancelResult;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.service.dto.command.PaymentCancelCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentConfirmCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentCreateCommand;
import com.example.order_service.payment.application.service.dto.result.PaymentConfirmResult;
import com.example.order_service.payment.application.service.dto.result.PaymentCreateResult;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.application.service.fixture.PaymentCommandFixture;
import com.example.order_service.payment.application.service.fixture.PaymentOrderResultFixture;
import com.example.order_service.payment.application.service.fixture.PaymentPGResultFixture;
import com.example.order_service.payment.application.service.fixture.PaymentResultFixture;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.domain.context.ApprovePaymentContext;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CancelPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

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

    @BeforeEach
    void setUp() {
        PaymentValidator validator = new PaymentValidator();
        PGErrorPolicy pgErrorPolicy = new PGErrorPolicy();
        PaymentContextFactory contextFactory = new PaymentContextFactory();
        paymentFacade = new PaymentFacade(paymentCommandService, paymentQueryService, paymentOrderPort, paymentPGPort, validator,
                pgErrorPolicy, contextFactory);
    }

    @Test
    @DisplayName("결제를 생성한다.")
    void create() {
        //given
        Long paymentId = 1L;
        PaymentCreateCommand command = PaymentCommandFixture.anCreateCommand().build();

        PaymentOrderResult orderResult = PaymentOrderResultFixture.anPaymentOrder().build();

        given(paymentOrderPort.getOrder(anyLong(), anyLong())).willReturn(orderResult);
        given(paymentCommandService.create(any(CreatePaymentContext.class))).willReturn(paymentId);
        //when
        PaymentCreateResult result = paymentFacade.create(command);
        //then
        assertThat(result.paymentId()).isNotNull();
    }

    @Test
    @DisplayName("결제를 승인한다.")
    void approve() {
        //given
        PaymentConfirmCommand command = PaymentCommandFixture.anConfirmCommand().build();

        PaymentResult paymentResult = PaymentResultFixture.anPaymentResult()
                .status(PaymentStatus.APPROVAL_PENDING)
                .transactions(Collections.emptyList())
                .method(null)
                .build();

        PGConfirmResult pgResult = PaymentPGResultFixture.anPGConfirmResult().build();

        given(paymentQueryService.getPayment(anyLong(), anyLong())).willReturn(paymentResult);
        willDoNothing()
                .given(paymentCommandService)
                .approvePending(anyLong(), any(ApprovePendingPaymentContext.class));
        given(paymentPGPort.confirm(anyLong(), anyString(), any(), any()))
                .willReturn(pgResult);
        //when
        PaymentConfirmResult result = paymentFacade.approve(command);
        //then
        assertThat(result.paymentId()).isEqualTo(1L);
        verify(paymentCommandService).approve(anyLong(), any(ApprovePaymentContext.class));
    }

    @Test
    @DisplayName("pg 결제 승인이 실패한 경우 결제를 실패로 변경하고 예외를 던진다.")
    void approve_whenPgApproveFailed_thenAbortAndThrownException() {
        //given
        PaymentConfirmCommand command = PaymentCommandFixture.anConfirmCommand().build();

        PaymentResult paymentResult = PaymentResultFixture.anPaymentResult()
                .status(PaymentStatus.APPROVAL_PENDING)
                .transactions(Collections.emptyList())
                .method(null)
                .build();

        given(paymentQueryService.getPayment(anyLong(), anyLong())).willReturn(paymentResult);
        willDoNothing()
                .given(paymentCommandService)
                .approvePending(anyLong(), any(ApprovePendingPaymentContext.class));
        given(paymentPGPort.confirm(anyLong(), anyString(), any(), any()))
                .willThrow(new PortException(PaymentPGPortErrorCode.PG_METHOD_REJECTED));
        //when
        //then
        assertThatThrownBy(() -> paymentFacade.approve(command))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_METHOD_REJECTED);

        verify(paymentCommandService, never()).approve(anyLong(), any());
        verify(paymentCommandService).abort(anyLong(), any());
    }

    @Test
    @DisplayName("결제 도메인 저장중 예외가 발생하면 PG 망취소를 호출하고 결제를 실패한다.")
    void approve_whenDbApproveFails_thenExecuteNetCancelAndAbort() {
        //given
        PaymentConfirmCommand command = PaymentCommandFixture.anConfirmCommand().build();
        PGConfirmResult pgResult = PaymentPGResultFixture.anPGConfirmResult().build();
        PaymentResult paymentResult = PaymentResultFixture.anPaymentResult()
                .status(PaymentStatus.APPROVAL_PENDING)
                .transactions(Collections.emptyList())
                .method(null)
                .build();

        given(paymentQueryService.getPayment(anyLong(), anyLong())).willReturn(paymentResult);
        willDoNothing().given(paymentCommandService).approvePending(anyLong(), any());
        given(paymentPGPort.confirm(anyLong(), anyString(), any(), any())).willReturn(pgResult);

        willThrow(new RuntimeException("DB 저장 실패"))
                .given(paymentCommandService).approve(anyLong(), any());

        willDoNothing().given(paymentPGPort).netCancel(anyString(), anyString(), any());
        willDoNothing().given(paymentCommandService).abort(anyLong(), any());
        //when
        //then
        assertThatThrownBy(() -> paymentFacade.approve(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 실패");

        then(paymentPGPort).should(times(1)).netCancel(eq(command.paymentKey()), anyString(), eq(command.provider()));
        then(paymentCommandService).should(times(1)).abort(eq(paymentResult.paymentId()), any());
    }

    @Test
    @DisplayName("pg 망취소 과정이 실패된 경우 예외가 발생한다.")
    void approve_whenNetCancelFails_thenThrownException() {
        //given
        PaymentConfirmCommand command = PaymentCommandFixture.anConfirmCommand().build();
        PGConfirmResult pgResult = PaymentPGResultFixture.anPGConfirmResult().build();
        PaymentResult paymentResult = PaymentResultFixture.anPaymentResult()
                .status(PaymentStatus.APPROVAL_PENDING)
                .transactions(Collections.emptyList())
                .method(null)
                .build();

        given(paymentQueryService.getPayment(anyLong(), anyLong())).willReturn(paymentResult);
        willDoNothing().given(paymentCommandService).approvePending(anyLong(), any());
        given(paymentPGPort.confirm(anyLong(), anyString(), any(), any())).willReturn(pgResult);

        willThrow(new RuntimeException("DB 저장 실패"))
                .given(paymentCommandService).approve(anyLong(), any());

        willThrow(new PortException(PaymentPGPortErrorCode.PG_CANCEL_REJECTED))
                .given(paymentPGPort).netCancel(anyString(), anyString(), any());
        //when
        //then
        assertThatThrownBy(() -> paymentFacade.approve(command))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentErrorCode.PAYMENT_REFUND_PENDING);

        then(paymentPGPort).should(times(1)).netCancel(eq(command.paymentKey()), anyString(), eq(command.provider()));
    }
    
    @Test
    @DisplayName("취소할 결제가 없는 경우 스킵된다")
    void cancel_whenPaymentNotFound_thenSkip() {
        //given
        PaymentCancelCommand command = PaymentCommandFixture.anCancelCommand().build();

        given(paymentQueryService.findCompletedPaymentByOrderId(anyLong())).willReturn(Optional.empty());
        //when
        paymentFacade.cancel(command);
        //then
        verify(paymentCommandService, never()).refundPending(anyLong());
    }

    @Test
    @DisplayName("취소할 결제가 존재하는 경우 결제 취소를 진행한다.")
    void cancel_whenPaymentExist_thenCancel() {
        //given
        PaymentResult paymentResult = PaymentResultFixture.anPaymentResult().build();
        PGCancelResult cancelResult = PaymentPGResultFixture.anPGCancelResult().build();

        PaymentCancelCommand command = PaymentCommandFixture.anCancelCommand().build();

        given(paymentQueryService.findCompletedPaymentByOrderId(anyLong())).willReturn(Optional.of(paymentResult));
        given(paymentPGPort.cancel(anyString(), anyString(), any(PaymentProvider.class))).willReturn(cancelResult);
        //when
        paymentFacade.cancel(command);
        //then
        verify(paymentCommandService).cancel(anyLong(), any(CancelPaymentContext.class));
    }
}