package com.example.order_service.payment.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.payment.application.external.PaymentGateway;
import com.example.order_service.payment.application.external.dto.command.PGPaymentCommand;
import com.example.order_service.payment.application.external.dto.result.PgPaymentResult;
import com.example.order_service.payment.application.external.mapper.PgMapper;
import com.example.order_service.payment.exception.PaymentErrorCode;
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
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayTest {

    @InjectMocks
    private PaymentGateway paymentGateway;
    @Mock
    private TossAdaptor adaptor;
    @Mock
    private PgMapper pgMapper;

    @Nested
    @DisplayName("결제 승인")
    class Confirm {
        @Test
        @DisplayName("결제를 승인한다")
        void confirm() {
            //given
            PGPaymentCommand.Confirm command = fixtureMonkey.giveMeOne(PGPaymentCommand.Confirm.class);
            TossClientResponse.Confirm response = fixtureMonkey.giveMeOne(TossClientResponse.Confirm.class);
            PgPaymentResult.Approval approval = fixtureMonkey.giveMeOne(PgPaymentResult.Approval.class);
            given(adaptor.confirmPayment(anyString(), anyString(), anyLong())).willReturn(response);
            given(pgMapper.toResult(any(TossClientResponse.Confirm.class))).willReturn(approval);
            //when
            PgPaymentResult.Approval confirm = paymentGateway.confirm(command);
            //then
            assertThat(confirm).isEqualTo(approval);
        }

        @Test
        @DisplayName("결제 승인중 토스 서버에서 오류가 발생한 경우 비지니스 예외가 발생한다")
        void confirm_externalServerException() {
            //given
            PGPaymentCommand.Confirm command = fixtureMonkey.giveMeOne(PGPaymentCommand.Confirm.class);
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "처리중 오류가 발생했습니다"))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_TOSS_SERVER_ERROR);
        }

        @Test
        @DisplayName("결제 승인중 토스 서버에서 클라이언트 에러가 발생한 경우 비지니스 예외가 발생한다")
        void confirm_externalClientException() {
            //given
            PGPaymentCommand.Confirm command = fixtureMonkey.giveMeOne(PGPaymentCommand.Confirm.class);
            willThrow(new ExternalClientException("TOSS_CLIENT_ERROR", "오류가 발생했습니다"))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_TOSS_CLIENT_ERROR);
        }

        @Test
        @DisplayName("결제 승인중 토스 서버 접근 불가 에러가 발생한 경우 비지니스 예외가 발생한다")
        void confirm_unavailableServerException() {
            //given
            PGPaymentCommand.Confirm command = fixtureMonkey.giveMeOne(PGPaymentCommand.Confirm.class);
            willThrow(new ExternalSystemUnavailableException("UNAVAILABLE", "오류가 발생했습니다"))
                    .given(adaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> paymentGateway.confirm(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_TOSS_UNAVAILABLE_ERROR);
        }
    }
}
