package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.common.exception.business.code.PaymentErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.TossAdaptor;
import com.example.order_service.infrastructure.dto.response.TossClientResponse;
import com.example.order_service.order.application.dto.result.OrderPaymentResult;
import com.example.order_service.order.application.dto.result.PaymentMethod;
import com.example.order_service.order.application.dto.result.PaymentStatus;
import com.example.order_service.order.application.mapper.OrderPaymentMapper;
import com.example.order_service.order.domain.service.dto.result.OrderPaymentInfo;
import com.example.order_service.order.infrastructure.client.payment.TossPaymentAdaptor;
import com.example.order_service.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import com.example.order_service.support.TestFixtureUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.api.support.fixture.order.OrderPaymentFixture.*;
import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderPaymentGatewayTest {

    @InjectMocks
    private OrderPaymentGateway orderPaymentGateway;
    @Mock
    private TossPaymentAdaptor tossPaymentAdaptor;
    @Mock
    private TossAdaptor tossAdaptor;
    @Spy
    private OrderPaymentMapper orderPaymentMapper = Mappers.getMapper(OrderPaymentMapper.class);

    @Nested
    @DisplayName("결제 승인 요청")
    class ConfirmOrderPayment {

        @Test
        @DisplayName("주문 결제를 승인한다")
        void confirmOrderPayment(){
            //given
            TossClientResponse.Confirm response = fixtureMonkey.giveMeBuilder(TossClientResponse.Confirm.class)
                    .set("status", "DONE")
                    .set("method", "카드")
                    .sample();
            given(tossAdaptor.confirmPayment(anyString(), anyString(), anyLong()))
                    .willReturn(response);
            //when
            OrderPaymentResult.Payment result = orderPaymentGateway.confirmOrderPayment("orderNo", "paymentKey", 10000L);
            //then
            assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
            assertThat(result.method()).isEqualTo(PaymentMethod.CARD);
        }

        @Test
        @DisplayName("주문 결제 승인 중 서버 오류가 발생한 경우 비지니스 예외로 변환하여 던진다")
        void confirmOrderPayment_externalServerException(){
            //given
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "결제 승인 처리중 오류 발생"))
                    .given(tossAdaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderPaymentGateway.confirmOrderPayment("orderNo", "paymentKey", 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PAYMENT_SERVER_ERROR);
        }

        @Test
        @DisplayName("주문 결제 승인 중 클라이언트 오류가 발생한 경우 비지니스 예외로 변환하여 던진다")
        void confirmOrderPayment_externalClientException(){
            //given
            willThrow(new ExternalClientException("ALREADY_PAYMENT", "이미 완료된 결제"))
                    .given(tossAdaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderPaymentGateway.confirmOrderPayment("orderNo", "paymentKey", 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PAYMENT_CLIENT_ERROR);
        }

        @Test
        @DisplayName("쿠폰 조회중 서비스 불가 오류가 발생한 경우 비지니스 예외로 변환해 던진다")
        void getCoupon_ExternalUnavailableException(){
            //given
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "토스 페이먼츠 통신장애"))
                    .given(tossAdaptor).confirmPayment(anyString(), anyString(), anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderPaymentGateway.confirmOrderPayment("orderNo", "paymentKey", 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PAYMENT_UNAVAILABLE_SERVER_ERROR);
        }

        @Test
        @DisplayName("주문 결제를 승인한다")
        void confirmOrderPaymentdeprecated(){
            //given
            OrderPaymentInfo expectedResult = anOrderPaymentInfo().build();
            TossPaymentConfirmResponse response = anTossPaymentResponse().build();
            given(tossPaymentAdaptor.confirmPayment(anyString(), anyString(), anyLong()))
                    .willReturn(response);
            //when
            OrderPaymentInfo result = orderPaymentGateway.confirmOrderPaymentdeprecated(ORDER_NO, "paymentKey", 7000L);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringFields("approvedAt")
                    .isEqualTo(expectedResult);
            assertThat(result.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 승인 상태가 완료 또는 가상 계좌 입금 이외의 상태이면 예외를 던진다")
        void confirmOrderPayment_status_other_than_done_or_waiting_deposit(){
            //given
            TossPaymentConfirmResponse response = anTossPaymentResponse().status("ABORT").build();
            given(tossPaymentAdaptor.confirmPayment(anyString(), anyString(), anyLong()))
                    .willReturn(response);
            //when
            //then
            assertThatThrownBy(() -> orderPaymentGateway.confirmOrderPaymentdeprecated(ORDER_NO, "paymentKey", 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_APPROVAL_FAIL);
        }
    }
}
