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
import com.example.order_service.payment.domain.model.PaymentStatus;
import com.example.order_service.payment.exception.PaymentErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.CannotAcquireLockException;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class PaymentFacadeTest {
    @InjectMocks
    private PaymentFacade paymentFacade;
    @Mock
    private PaymentQueryService paymentQueryService;
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
                    .userId(1L).orderNo("orderNo").paymentKey("paymentKey")
                    .amount(Money.wons(10000L)).build();
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PENDING)
                    .set(field("totalPaymentAmount"), Money.wons(10000L))
                    .create();
            PaymentContext.Create createContext = Instancio.create(PaymentContext.Create.class);
            PaymentResult.Default savedPayment = Instancio.create(PaymentResult.Default.class);
            PgPaymentResult.Approval pgApproval = Instancio.create(PgPaymentResult.Approval.class);
            PaymentContext.Approval approvalContext = Instancio.create(PaymentContext.Approval.class);
            PaymentResult.PaymentApproval approvalResult = Instancio.create(PaymentResult.PaymentApproval.class);
            given(orderQueryService.getOrder(anyString(), anyLong())).willReturn(order);
            given(mapper.toContext(any(PaymentCommand.Confirm.class))).willReturn(createContext);
            given(paymentCommandService.create(any(PaymentContext.Create.class))).willReturn(savedPayment);
            given(paymentGateway.confirm(any())).willReturn(pgApproval);
            given(mapper.toContext(anyLong(), any(PgPaymentResult.Approval.class))).willReturn(approvalContext);
            given(paymentCommandService.approve(any())).willReturn(approvalResult);
            //when
            PaymentResult.PaymentApproval confirm = paymentFacade.confirm(command);
            //then
            assertThat(confirm).isEqualTo(approvalResult);

            InOrder inOrder = inOrder(
                    orderQueryService, paymentCommandService, paymentGateway, mapper
            );
            inOrder.verify(orderQueryService).getOrder("orderNo", 1L);
            inOrder.verify(mapper).toContext(command);
            inOrder.verify(paymentCommandService).create(createContext);
            inOrder.verify(paymentGateway).confirm(any());
            inOrder.verify(paymentCommandService).approve(approvalContext);
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
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PAID)
                    .create();
            given(orderQueryService.getOrder(anyString(), anyLong()))
                    .willReturn(order);
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.ORDER_NOT_PENDING);

            verifyNoInteractions(paymentCommandService);
            verifyNoInteractions(paymentGateway);
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
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PENDING)
                    .set(field("totalPaymentAmount"), Money.wons(4000L))
                    .create();
            given(orderQueryService.getOrder(anyString(), anyLong()))
                    .willReturn(order);
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);

            verifyNoInteractions(paymentCommandService);
            verifyNoInteractions(paymentGateway);
        }

        @Test
        @DisplayName("토스 결제 승인중 클라이언트 에러가 발생한 경우 결제를 취소한다")
        void confirm_PG_client_error(){
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PENDING)
                    .set(field("totalPaymentAmount"), Money.wons(10000L))
                    .create();
            PaymentContext.Create createContext = Instancio.create(PaymentContext.Create.class);
            PaymentResult.Default savedPayment = Instancio.create(PaymentResult.Default.class);
            given(orderQueryService.getOrder(anyString(), anyLong())).willReturn(order);
            given(mapper.toContext(any(PaymentCommand.Confirm.class))).willReturn(createContext);
            given(paymentCommandService.create(any(PaymentContext.Create.class))).willReturn(savedPayment);
            willThrow(new BusinessException(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE)).given(paymentGateway).confirm(any());
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE);

            verify(paymentCommandService).abort(anyLong(), anyString());
        }

        @Test
        @DisplayName("PG사 결제 실패 후 DB 상태 갱신(fail) 중 에러가 발생해도, 원래의 PG 에러를 정상적으로 던진다")
        void confirm_PgErrorWithDbSaveFailure_ThrowsOriginalPgError(){
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PENDING)
                    .set(field("totalPaymentAmount"), Money.wons(10000L))
                    .create();
            PaymentContext.Create createContext = Instancio.create(PaymentContext.Create.class);
            PaymentResult.Default savedPayment = Instancio.create(PaymentResult.Default.class);
            given(orderQueryService.getOrder(anyString(), anyLong())).willReturn(order);
            given(mapper.toContext(any(PaymentCommand.Confirm.class))).willReturn(createContext);
            given(paymentCommandService.create(any(PaymentContext.Create.class))).willReturn(savedPayment);
            willThrow(new BusinessException(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE)).given(paymentGateway).confirm(any());
            willThrow(new RuntimeException()).given(paymentCommandService).abort(anyLong(), anyString());
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("PG 승인 성공 후 Payment 저장시 알 수 없는 예외가 발생하면 망취소를 수행한 뒤 예외가 발생한다")
        void confirm_whenPaymentDoneUnKnownFailAfterPGApproval_thenExecuteNetworkCancel(){
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PENDING)
                    .set(field("totalPaymentAmount"), Money.wons(10000L))
                    .create();
            PaymentContext.Create createContext = Instancio.create(PaymentContext.Create.class);
            PaymentResult.Default savedPayment = Instancio.create(PaymentResult.Default.class);
            PgPaymentResult.Approval pgApproval = Instancio.create(PgPaymentResult.Approval.class);
            PaymentContext.Approval approvalContext = Instancio.create(PaymentContext.Approval.class);
            PgPaymentResult.Cancellation cancellation = Instancio.create(PgPaymentResult.Cancellation.class);
            given(orderQueryService.getOrder(anyString(), anyLong())).willReturn(order);
            given(mapper.toContext(any(PaymentCommand.Confirm.class))).willReturn(createContext);
            given(paymentCommandService.create(any(PaymentContext.Create.class))).willReturn(savedPayment);
            given(paymentGateway.confirm(any())).willReturn(pgApproval);
            given(mapper.toContext(anyLong(), any(PgPaymentResult.Approval.class))).willReturn(approvalContext);
            willThrow(new RuntimeException()).given(paymentCommandService).approve(any());
            given(paymentGateway.cancel(any())).willReturn(cancellation);
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_AUTO_CANCELED);
            verify(paymentGateway).cancel(any());
            verify(paymentCommandService).abort(anyLong(), anyString());
        }
        
        @Test
        @DisplayName("망취소 성공 후 환불 record 저장중 예외가 발생하여도 망 취소 결과에 대한 예외가 던져진다")
        void confirm_whenSuccessNetworkCancel_thenPaymentCancelFail() {
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PENDING)
                    .set(field("totalPaymentAmount"), Money.wons(10000L))
                    .create();
            PaymentContext.Create createContext = Instancio.create(PaymentContext.Create.class);
            PaymentResult.Default savedPayment = Instancio.create(PaymentResult.Default.class);
            PgPaymentResult.Approval pgApproval = Instancio.create(PgPaymentResult.Approval.class);
            PaymentContext.Approval approvalContext = Instancio.create(PaymentContext.Approval.class);
            PgPaymentResult.Cancellation cancellation = Instancio.create(PgPaymentResult.Cancellation.class);
            given(orderQueryService.getOrder(anyString(), anyLong())).willReturn(order);
            given(mapper.toContext(any(PaymentCommand.Confirm.class))).willReturn(createContext);
            given(paymentCommandService.create(any(PaymentContext.Create.class))).willReturn(savedPayment);
            given(paymentGateway.confirm(any())).willReturn(pgApproval);
            given(mapper.toContext(anyLong(), any(PgPaymentResult.Approval.class))).willReturn(approvalContext);
            willThrow(new RuntimeException()).given(paymentCommandService).approve(any());
            given(paymentGateway.cancel(any())).willReturn(cancellation);
            willThrow(new RuntimeException()).given(paymentCommandService).abort(anyLong(), anyString());
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_AUTO_CANCELED);
            verify(paymentGateway).cancel(any());
        }

        @Test
        @DisplayName("망취소에 실패하면 예외가 발생한다")
        void confirm_throwException_whenNetworkCancelFails(){
            //given
            PaymentCommand.Confirm command = PaymentCommand.Confirm.builder()
                    .userId(1L)
                    .orderNo("orderNo")
                    .paymentKey("paymentKey")
                    .amount(Money.wons(10000L))
                    .build();
            OrderResult.Detail order = Instancio.of(OrderResult.Detail.class)
                    .set(field("status"), OrderStatus.PENDING)
                    .set(field("totalPaymentAmount"), Money.wons(10000L))
                    .create();
            PaymentContext.Create createContext = Instancio.create(PaymentContext.Create.class);
            PaymentResult.Default savedPayment = Instancio.create(PaymentResult.Default.class);
            PgPaymentResult.Approval pgApproval = Instancio.create(PgPaymentResult.Approval.class);
            PaymentContext.Approval approvalContext = Instancio.create(PaymentContext.Approval.class);
            given(orderQueryService.getOrder(anyString(), anyLong())).willReturn(order);
            given(mapper.toContext(any(PaymentCommand.Confirm.class))).willReturn(createContext);
            given(paymentCommandService.create(any(PaymentContext.Create.class))).willReturn(savedPayment);
            given(paymentGateway.confirm(any())).willReturn(pgApproval);
            given(mapper.toContext(anyLong(), any(PgPaymentResult.Approval.class))).willReturn(approvalContext);
            willThrow(new RuntimeException()).given(paymentCommandService).approve(any());
            willThrow(new BusinessException(PaymentErrorCode.PAYMENT_TOSS_CLIENT_ERROR)).given(paymentGateway).cancel(any());
            //when
            //then
            assertThatThrownBy(() -> paymentFacade.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_REFUND_PENDING);
            verify(paymentGateway).cancel(any());
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class PaymentRevert {

        @Test
        @DisplayName("결제를 취소한다")
        void revert(){
            //given
            Long paymentId = 1L;
            String reason = "INSUFFICIENT_STOCK";
            PaymentResult.Default payment = Instancio.create(PaymentResult.Default.class);
            PgPaymentResult.Cancellation cancellation = Instancio.create(PgPaymentResult.Cancellation.class);
            PaymentContext.Cancellation context = Instancio.create(PaymentContext.Cancellation.class);
            PaymentResult.PaymentCancel paymentCancel = Instancio.create(PaymentResult.PaymentCancel.class);
            given(paymentQueryService.getPayment(anyLong())).willReturn(payment);
            doNothing().when(paymentCommandService).changeRefundPending(anyLong());
            given(paymentGateway.cancel(any())).willReturn(cancellation);
            given(mapper.toContext(anyLong(), any(), any())).willReturn(context);
            given(paymentCommandService.cancel(any())).willReturn(paymentCancel);
            //when
            paymentFacade.revert(paymentId, reason);
            //then
            verify(paymentCommandService).cancel(any());
        }

        @Test
        @DisplayName("PG 통신중 예외가 발생하면 예외를 삼키고 정상 종료한다")
        void revert_swallows_exception_on_pg_fail(){
            //given
            Long paymentId = 1L;
            String reason = "INSUFFICIENT_STOCK";
            PaymentResult.Default payment = Instancio.create(PaymentResult.Default.class);

            given(paymentQueryService.getPayment(anyLong())).willReturn(payment);
            doNothing().when(paymentCommandService).changeRefundPending(anyLong());
            given(paymentGateway.cancel(any())).willThrow(new RuntimeException("PG Timeout"));
            //when
            //then
            assertDoesNotThrow(() -> paymentFacade.revert(paymentId, reason));
            verify(paymentCommandService, never()).cancel(any());
        }
    }
}
