package com.example.order_service.payment.external;

import com.example.order_service.common.exception.application.GatewayRejectException;
import com.example.order_service.common.exception.application.PaymentUnknownStateException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgErrorTranslator;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.exception.PaymentErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayTest {

    @InjectMocks
    private PaymentGateway paymentGateway;
    @Mock
    private TossAdaptor adaptor;
    @Mock
    private PgMapper pgMapper;
    @Mock
    private PgErrorTranslator errorTranslator;

    @Nested
    @DisplayName("결제 승인")
    class Confirm {

        @Test
        @DisplayName("결제 승인 요청이 성공하면 승인 결과를 반환한다")
        void confirm() {
            //given
            PGPaymentCommand.Confirm command = Instancio.create(PGPaymentCommand.Confirm.class);
            TossClientResponse.Confirm response = Instancio.create(TossClientResponse.Confirm.class);
            PGPaymentResult.Approval approval = Instancio.create(PGPaymentResult.Approval.class);
            given(adaptor.confirmPayment(anyString(), anyString(), anyLong())).willReturn(response);
            given(pgMapper.toResult(any(TossClientResponse.Confirm.class))).willReturn(approval);
            //when
            PGPaymentResult.Approval confirm = paymentGateway.confirm(command);
            //then
            assertThat(confirm).isEqualTo(approval);
        }

        @Test
        @DisplayName("결제 승인 시 결제 상태를 알 수 없는 에러가 발생하면 Unknown 예외를 발생시킨다")
        void confirm_pg_status_unknown_exception() {
            //given
            PGPaymentCommand.Confirm command = Instancio.create(PGPaymentCommand.Confirm.class);
            String code = "FORBIDDEN_CONSECUTIVE_REQUEST";
            String message = "반복적인 요청 제한";
            willThrow(new ExternalServerException("FORBIDDEN_CONSECUTIVE_REQUEST", "반복적인 요청 제한"))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            given(errorTranslator.translate(code)).willReturn(PaymentErrorCode.PAYMENT_PG_SERVER_ERROR);
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(PaymentUnknownStateException.class)
                    .extracting("errorCode", "code", "message")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_SERVER_ERROR, code, message);
        }

        @Test
        @DisplayName("결제 승인 시 결제가 확정 실패된 경우 Reject 예외를 발생시킨다")
        void confirm_pg_reject() {
            //given
            PGPaymentCommand.Confirm command = Instancio.create(PGPaymentCommand.Confirm.class);
            String code = "REJECT_ACCOUNT_PAYMENT";
            String message = "잔액이 부족합니다.";
            willThrow(new ExternalClientException(code, message))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            given(errorTranslator.translate(code)).willReturn(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE);
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(GatewayRejectException.class)
                    .extracting("errorCode", "code", "message")
                    .containsExactly(PaymentErrorCode.PAYMENT_INSUFFICIENT_BALANCE, code, message);
        }

        @Test
        @DisplayName("결제 승인 시 서킷 브레이커가 열려 있으면 Reject 예외를 발생시킨다")
        void confirm_externalCircuitBreakerException() {
            //given
            String code = "CIRCUIT_OPEN";
            String message = "서킷 브레이커 열림";
            PGPaymentCommand.Confirm command = Instancio.create(PGPaymentCommand.Confirm.class);
            willThrow(new ExternalCircuitBreakerException(code, message))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(GatewayRejectException.class)
                    .extracting("errorCode", "code", "message")
                    .containsExactly(PaymentErrorCode.PAYMENT_TOSS_CIRCUIT_OPEN, code, message);
        }

        @Test
        @DisplayName("결제 승인 시 PG와 통신할 수 없으면 Unknown 예외를 발생시킨다")
        void confirm_externalUnavailableException(){
            //given
            String code = "SERVICE_UNAVAILABLE";
            String message = "통신 장애";
            PGPaymentCommand.Confirm command = Instancio.create(PGPaymentCommand.Confirm.class);
            willThrow(new ExternalSystemUnavailableException(code, message))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(PaymentUnknownStateException.class)
                    .extracting("errorCode", "code", "message")
                    .containsExactly(PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR, code, message);
        }
    }

    @Nested
    @DisplayName("환불")
    class Cancel {

        @Test
        @DisplayName("결제 취소 요청이 성공하면 취소 결과를 반환한다")
        void cancel() {
            //given
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            PGPaymentResult.Cancellation result = Instancio.create(PGPaymentResult.Cancellation.class);
            TossClientResponse.Cancel response = Instancio.create(TossClientResponse.Cancel.class);
            given(adaptor.cancelPayment(anyString(), anyString(), anyLong())).willReturn(response);
            given(pgMapper.toResult(any(TossClientResponse.Cancel.class))).willReturn(result);
            //when
            PGPaymentResult.Cancellation cancel = paymentGateway.cancel(command);
            //then
            assertThat(cancel).isEqualTo(result);
        }

        @Test
        @DisplayName("결제 취소 시 PG 서버 오류가 발생하면 결제 상태를 알 수 없는 예외를 발생시킨다")
        void cancel_externalServerException() {
            //given
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "처리중 오류가 발생했습니다"))
                    .given(adaptor).cancelPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.cancel(command))
                    .isInstanceOf(PaymentUnknownStateException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_TOSS_SERVER_ERROR);
        }

        @Test
        @DisplayName("결제 취소 시 PG가 요청을 거절하면 요청 거절 예외를 발생시킨다")
        void cancel_externalClientException() {
            //given
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            willThrow(new ExternalClientException("TOSS_CLIENT_ERROR", "오류가 발생했습니다"))
                    .given(adaptor).cancelPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.cancel(command))
                    .isInstanceOf(GatewayRejectException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_TOSS_CLIENT_ERROR);
        }

        @Test
        @DisplayName("결제 취소 시 서킷 브레이커가 열려 있으면 요청 거절 예외를 발생시킨다")
        void cancel_externalCircuitBreakerException(){
            //given
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            willThrow(new ExternalCircuitBreakerException("CIRCUIT_OPEN", "서킷 브레이커 열림"))
                    .given(adaptor).cancelPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.cancel(command))
                    .isInstanceOf(GatewayRejectException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_TOSS_CIRCUIT_OPEN);
        }

        @Test
        @DisplayName("결제 취소 시 PG와 통신할 수 없으면 결제 상태를 알 수 없는 예외를 발생시킨다")
        void cancel_unavailableServerException() {
            //given
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            willThrow(new ExternalSystemUnavailableException("UNAVAILABLE", "오류가 발생했습니다"))
                    .given(adaptor).cancelPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.cancel(command))
                    .isInstanceOf(PaymentUnknownStateException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR);
        }
    }
}
