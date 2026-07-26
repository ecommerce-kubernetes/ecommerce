package com.example.order_service.payment.external;

import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.infrastructure.gateway.TossGateway;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PGPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgErrorTranslator;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.exception.PaymentErrorCode;
import com.example.order_service.payment.exception.PaymentGatewayException;
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
    private TossGateway adaptor;
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
        @DisplayName("결제 승인 요청시 외부 시스템 예외가 발생한 경우 에러 코드를 매핑하여 예외를 변환한다")
        void confirm_External_System_exception() {
            //given
            PGPaymentCommand.Confirm command = Instancio.create(PGPaymentCommand.Confirm.class);
            String code = "FORBIDDEN_CONSECUTIVE_REQUEST";
            String message = "반복적인 요청 제한";
            willThrow(new ExternalServerException(code, message))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            given(errorTranslator.translate(code)).willReturn(PaymentErrorCode.PAYMENT_PG_SERVER_ERROR);
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_SERVER_ERROR, code);
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
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_CIRCUIT_OPEN, code);
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
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_UNAVAILABLE_ERROR, code);
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
        @DisplayName("결제 취소 시 외부 시스템 에러가 발생한 경우 에러 코드를 매핑하여 예외를 변환한다")
        void cancel_External_System_exception() {
            //given
            String code = "PROVIDER_ERROR";
            String message = "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            willThrow(new ExternalClientException(code, message))
                    .given(adaptor).cancelPayment(anyString(), anyString(), anyLong());
            given(errorTranslator.translate(any())).willReturn(PaymentErrorCode.PAYMENT_PG_SERVER_ERROR);
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.cancel(command))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("결제 취소 시 서킷 브레이커가 열려 있으면 요청 거절 예외를 발생시킨다")
        void cancel_externalCircuitBreakerException(){
            //given
            String code = "CIRCUIT_OPEN";
            String message = "서킷 브레이커 열림";
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            willThrow(new ExternalCircuitBreakerException(code, message))
                    .given(adaptor).cancelPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.cancel(command))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_CIRCUIT_OPEN, code);
        }

        @Test
        @DisplayName("결제 취소 시 PG와 통신할 수 없으면 결제 상태를 알 수 없는 예외를 발생시킨다")
        void cancel_unavailableServerException() {
            //given
            String code = "UNAVAILABLE";
            String message = "오류가 발생했습니다";
            PGPaymentCommand.Cancel command = Instancio.create(PGPaymentCommand.Cancel.class);
            willThrow(new ExternalSystemUnavailableException(code, message))
                    .given(adaptor).cancelPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.cancel(command))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_UNAVAILABLE_ERROR, code);
        }
    }

    @Nested
    @DisplayName("결제 조회")
    class Inquiry {

        @Test
        @DisplayName("결제를 조회한다")
        void inquire(){
            //given
            String paymentKey = "paymentKey";
            TossClientResponse.Inquiry response = Instancio.create(TossClientResponse.Inquiry.class);
            PGPaymentResult.Inquiry result = Instancio.create(PGPaymentResult.Inquiry.class);
            given(adaptor.inquirePayment(anyString())).willReturn(response);
            given(pgMapper.toResult(any(TossClientResponse.Inquiry.class))).willReturn(result);
            //when
            PGPaymentResult.Inquiry inquire = paymentGateway.inquire(paymentKey);
            //then
            assertThat(inquire).isEqualTo(result);
        }

        @Test
        @DisplayName("결제 조회 시 외부 시스템 에러가 발생한 경우 에러 코드를 매핑하여 예외를 변환한다")
        void inquire_External_System_exception() {
            //given
            String code = "PAYMENT_NOT_FOUND";
            String message = "결제를 찾을 수 없습니다";
            String paymentKey = "paymentKey";
            willThrow(new ExternalClientException(code, message))
                    .given(adaptor).inquirePayment(anyString());
            given(errorTranslator.translate(any())).willReturn(PaymentErrorCode.PAYMENT_PG_NOT_FOUND);
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.inquire(paymentKey))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_NOT_FOUND, code);
        }

        @Test
        @DisplayName("결제 조회 시 서킷 브레이커가 열려 있으면 요청 거절 예외를 발생시킨다")
        void inquire_externalCircuitBreakerException(){
            //given
            String paymentKey = "paymentKey";
            String code = "CIRCUIT_OPEN";
            String message = "서킷 브레이커 열림";
            willThrow(new ExternalCircuitBreakerException(code, message))
                    .given(adaptor).inquirePayment(anyString());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.inquire(paymentKey))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_CIRCUIT_OPEN, code);
        }

        @Test
        @DisplayName("결제 조회 시 PG와 통신할 수 없으면 결제 상태를 알 수 없는 예외를 발생시킨다")
        void inquire_unavailableServerException() {
            //given
            String code = "UNAVAILABLE";
            String message = "오류가 발생했습니다";
            String paymentKey = "paymentKey";
            willThrow(new ExternalSystemUnavailableException(code, message))
                    .given(adaptor).inquirePayment(anyString());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.inquire(paymentKey))
                    .isInstanceOf(PaymentGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(PaymentErrorCode.PAYMENT_PG_UNAVAILABLE_ERROR, code);
        }
    }
}
