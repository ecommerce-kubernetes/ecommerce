package com.example.order_service.payment.infrastructure.adaptor.client.pg.toss;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.dto.response.pg.TossCancelResponse;
import com.example.order_service.infrastructure.dto.response.pg.TossConfirmResponse;
import com.example.order_service.infrastructure.gateway.TossGateway;
import com.example.order_service.payment.application.port.dto.PGConfirmResult;
import com.example.order_service.payment.application.port.dto.PaymentPGStatus;
import com.example.order_service.payment.domain.PaymentMethod;
import com.example.order_service.payment.domain.PaymentProvider;
import com.example.order_service.payment.exception.PaymentPGPortErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TossPGProcessorTest {

    @InjectMocks
    private TossPGProcessor tossPGProcessor;

    @Mock
    private TossGateway tossGateway;
    @Spy
    private TossErrorTranslator errorTranslator;
    @Spy
    private TossPGMapper mapper;

    @Test
    @DisplayName("지원 가능 결제사를 반환한다.")
    void getSupportedProvider() {
        //given
        //when
        PaymentProvider supportedProvider = tossPGProcessor.getSupportedProvider();
        //then
        assertThat(supportedProvider).isEqualTo(PaymentProvider.TOSS);
    }

    @Test
    @DisplayName("토스 결제를 승인한다.")
    void confirm() {
        //given
        Long orderId = 1L;
        String paymentKey = "paymentKey";
        Money amount = Money.wons(1000L);
        OffsetDateTime approvedAt = OffsetDateTime.now();
        LocalDateTime expectedApprovedAt = approvedAt.toLocalDateTime();
        TossConfirmResponse response = TossConfirmResponse.builder()
                .status("DONE")
                .totalAmount(1000L)
                .method("카드")
                .lastTransactionKey("transactionKey")
                .approvedAt(approvedAt)
                .build();

        given(tossGateway.confirmPayment(anyLong(), anyString(), anyLong()))
                .willReturn(response);

        //when
        PGConfirmResult result = tossPGProcessor.confirm(orderId, paymentKey, amount);
        //then
        assertThat(result.status()).isEqualTo(PaymentPGStatus.DONE);
        assertThat(result.amount()).isEqualTo(Money.wons(1000L));
        assertThat(result.method()).isEqualTo(PaymentMethod.CARD);
        assertThat(result.transactionKey()).isEqualTo("transactionKey");
        assertThat(result.approvedAt()).isEqualTo(expectedApprovedAt);
    }

    @Test
    @DisplayName("토스 결제 승인시 토스 클라이언트 예외가 던져진 경우 포트 예외로 번역한다")
    void confirm_ExternalClientException() {
        //given
        given(tossGateway.confirmPayment(anyLong(), anyString(), anyLong())).willThrow(new ExternalClientException("ALREADY_PROCESSED_PAYMENT", "이미 처리된 결제 입니다."));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.confirm(1L, "paymentKey", Money.wons(1000L)))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_ALREADY_PROCESSED);
    }

    @Test
    @DisplayName("토스 결제 승인시 토스 서버 예외가 던져진 경우 포트 예외로 번역한다.")
    void confirm_ExternalServerException() {
        //given
        given(tossGateway.confirmPayment(anyLong(), anyString(), anyLong())).willThrow(new ExternalServerException("UNKNOWN_PAYMENT_ERROR", "결제에 실패했어요. 같은 문제가 반복된다면 은행이나 카드사로 문의해주세요."));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.confirm(1L, "paymentKey", Money.wons(1000L)))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_SERVER_ERROR);
    }
    
    @Test
    @DisplayName("토스 결제 승인시 토스 서킷 브레이커 예외가 던져진 경우 포트 예외로 번역한다.")
    void confirm_ExternalCircuitBreakerException() {
        //given
        String code = "TOSS_CIRCUIT_OPEN";
        String message = "토스 서킷 브레이커 열림";
        given(tossGateway.confirmPayment(anyLong(), anyString(), anyLong())).willThrow(new ExternalCircuitBreakerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.confirm(1L, "paymentKey", Money.wons(1000L)))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_CIRCUIT_OPEN);
    }

    @Test
    @DisplayName("토스 결제 승인시 토스 서버 통신 불가 예외가 던져진 경우 포트 예외로 번역한다.")
    void confirm_ExternalUnavailableServerException() {
        //given
        String code = "SERVICE_UNAVAILABLE";
        String message = "토스 통신 장애";
        given(tossGateway.confirmPayment(anyLong(), anyString(), anyLong())).willThrow(new ExternalSystemUnavailableException(code, message));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.confirm(1L, "paymentKey", Money.wons(1000L)))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_UNAVAILABLE_ERROR);
    }

    @Test
    @DisplayName("토스 결제를 망취소 한다.")
    void netCancel(){
        //given
        String paymentKey = "paymentKey";
        String cancelReason = "망 취소";
        OffsetDateTime canceledAt = OffsetDateTime.now();

        TossCancelResponse.CancelReceipt receipt = TossCancelResponse.CancelReceipt.builder()
                .transactionKey("transactionKey")
                .cancelAmount(1000L)
                .canceledAt(canceledAt)
                .cancelReason("망 취소")
                .build();

        TossCancelResponse response = TossCancelResponse.builder()
                .status("CANCELED")
                .cancels(List.of(receipt))
                .build();

        given(tossGateway.cancelPayment(anyString(), anyString(), nullable(Long.class)))
                .willReturn(response);
        //when
        //then
        assertThatCode(() -> tossPGProcessor.netCancel(paymentKey, cancelReason))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("토스 결제 망취소시 토스 클라이언트 예외가 던져진 경우 포트 예외로 번역한다")
    void netCancel_ExternalClientException(){
        //given
        String paymentKey = "paymentKey";
        String cancelReason = "망 취소";

        given(tossGateway.cancelPayment(anyString(), anyString(), nullable(Long.class)))
                .willThrow(new ExternalClientException("ALREADY_CANCELED_PAYMENT", "이미 취소된 결제 입니다."));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.netCancel(paymentKey, cancelReason))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_ALREADY_CANCELED);
    }

    @Test
    @DisplayName("토스 결제 망취소시 토스 서버 예외가 던져진 경우 포트 예외로 번역한다.")
    void netCancel_ExternalServerException(){
        //given
        String paymentKey = "paymentKey";
        String cancelReason = "망 취소";

        given(tossGateway.cancelPayment(anyString(), anyString(), nullable(Long.class)))
                .willThrow(new ExternalServerException("FAILED_REFUND_PROCESS", "은행 응답시간 지연이나 일시적인 오류로 환불요청에 실패했습니다."));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.netCancel(paymentKey, cancelReason))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_SERVER_ERROR);
    }

    @Test
    @DisplayName("토스 결제 망취소시 토스 서킷 예외가 던져진 경우 포트 예외로 번역한다.")
    void netCancel_ExternalCircuitBreakerException(){
        //given
        String paymentKey = "paymentKey";
        String cancelReason = "망 취소";

        given(tossGateway.cancelPayment(anyString(), anyString(), nullable(Long.class)))
                .willThrow(new ExternalCircuitBreakerException("CIRCUIT_BREAKER_OPEN", "토스 서비스 서킷 차단"));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.netCancel(paymentKey, cancelReason))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_CIRCUIT_OPEN);
    }

    @Test
    @DisplayName("토스 결제 망취소시 토스 통신 불가 예외가 던져진 경우 포트 예외로 번역한다.")
    void netCancel_ExternalUnavailableServerException(){
        //given
        String paymentKey = "paymentKey";
        String cancelReason = "망 취소";

        given(tossGateway.cancelPayment(anyString(), anyString(), nullable(Long.class)))
                .willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "토스 통신 오류"));
        //when
        //then
        assertThatThrownBy(() -> tossPGProcessor.netCancel(paymentKey, cancelReason))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentPGPortErrorCode.PG_UNAVAILABLE_ERROR);
    }
}