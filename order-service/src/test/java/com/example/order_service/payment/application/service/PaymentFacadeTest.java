package com.example.order_service.payment.application.service;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderStatus;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.mapper.PaymentMapper;
import com.example.order_service.payment.application.service.dto.command.PaymentCommand;
import com.example.order_service.payment.application.service.dto.command.PaymentContext;
import com.example.order_service.payment.application.service.dto.result.PaymentResult;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PaymentFacadeTest {
    @InjectMocks
    private PaymentFacade paymentFacade;

    @Mock
    private OrderQueryService orderQueryService;
    @Mock
    private PaymentCommandService paymentCommandService;
    @Mock
    private PaymentMapper mapper;
    @Mock
    private PaymentGateway paymentGateway;

    @Nested
    @DisplayName("결제 승인")
    class PaymentConfirm {

        @Test
        @DisplayName("주문 결제를 승인한다")
        void confirm() {
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = fixtureMonkey.giveMeBuilder(OrderResult.Detail.class)
                    .set("status", OrderStatus.PENDING)
                    .set("totalPaymentAmount", Money.wons(10000L))
                    .sample();
            PaymentContext.Create create = fixtureMonkey.giveMeOne(PaymentContext.Create.class);
            PaymentResult.Default aDefault = fixtureMonkey.giveMeOne(PaymentResult.Default.class);
            PgPaymentResult.Approval approval = fixtureMonkey.giveMeOne(PgPaymentResult.Approval.class);
            PaymentContext.Approval approvalContext = fixtureMonkey.giveMeOne(PaymentContext.Approval.class);
            PaymentResult.PaymentApproval approvalResult = fixtureMonkey.giveMeOne(PaymentResult.PaymentApproval.class);
            given(orderQueryService.getOrder(anyString(), anyLong()))
                    .willReturn(order);
            given(mapper.toContext(any(PaymentCommand.Confirm.class)))
                    .willReturn(create);
            given(paymentCommandService.save(any(PaymentContext.Create.class)))
                    .willReturn(aDefault);
            given(paymentGateway.confirm(any())).willReturn(approval);
            given(mapper.toContext(anyLong(), any())).willReturn(approvalContext);
            given(paymentCommandService.approve(any())).willReturn(approvalResult);
            //when
            PaymentResult.PaymentApproval confirm = paymentFacade.confirm(command);
            //then
            assertThat(confirm).isEqualTo(approvalResult);
        }

        @Test
        @DisplayName("주문이 대기중 상태가 아니면 결제를 승인할 수 없다")
        void confirm_order_status_is_not_pending() {
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = fixtureMonkey.giveMeBuilder(OrderResult.Detail.class)
                    .set("status", OrderStatus.COMPLETED)
                    .sample();
            given(orderQueryService.getOrder(anyString(), anyLong()))
                    .willReturn(order);
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.ORDER_NOT_PENDING);
        }

        @Test
        @DisplayName("주문 결제 금액과 결제 승인 금액이 다르면 예외가 발생한다")
        void confirm_amount_misMatch() {
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = fixtureMonkey.giveMeBuilder(OrderResult.Detail.class)
                    .set("status", OrderStatus.PENDING)
                    .set("totalPaymentAmount", Money.wons(4000L))
                    .sample();
            given(orderQueryService.getOrder(anyString(), anyLong()))
                    .willReturn(order);
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

    }
}
