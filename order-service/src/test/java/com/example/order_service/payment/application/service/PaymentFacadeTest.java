package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.service.order.OrderContextFactory;
import com.example.order_service.order.application.service.order.OrderFacade;
import com.example.order_service.order.application.service.validator.OrderValidator;
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
import com.example.order_service.payment.application.service.fixture.PaymentCommandFixture;
import com.example.order_service.payment.application.service.fixture.PaymentOrderResultFixture;
import com.example.order_service.payment.application.service.fixture.PaymentPGResultFixture;
import com.example.order_service.payment.domain.PaymentFailure;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.domain.PaymentStatus;
import com.example.order_service.payment.domain.context.ApprovePendingPaymentContext;
import com.example.order_service.payment.domain.context.CreatePaymentContext;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.junit.jupiter.api.BeforeEach;
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
        PaymentConfirmCommand command = PaymentCommandFixture.anConfirmCommand().build();

        PGConfirmResult pgResult = PaymentPGResultFixture.anPGConfirmResult().build();

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
}