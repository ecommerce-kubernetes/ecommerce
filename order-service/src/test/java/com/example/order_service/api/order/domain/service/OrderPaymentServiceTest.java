package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.PaymentErrorCode;
import com.example.order_service.api.order.domain.service.dto.result.OrderPaymentInfo;
import com.example.order_service.api.order.infrastructure.client.payment.TossPaymentAdaptor;
import com.example.order_service.api.order.infrastructure.client.payment.dto.response.TossPaymentConfirmResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.api.support.fixture.OrderPaymentFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderPaymentServiceTest {

    @InjectMocks
    private OrderPaymentService orderPaymentService;
    @Mock
    private TossPaymentAdaptor tossPaymentAdaptor;

    @Nested
    @DisplayName("결제 승인 요청")
    class ConfirmOrderPayment {

        @Test
        @DisplayName("주문 결제를 승인한다")
        void confirmOrderPayment(){
            //given
            OrderPaymentInfo expectedResult = anOrderPaymentInfo().build();
            TossPaymentConfirmResponse response = anTossPaymentResponse().build();
            given(tossPaymentAdaptor.confirmPayment(anyString(), anyString(), anyLong()))
                    .willReturn(response);
            //when
            OrderPaymentInfo result = orderPaymentService.confirmOrderPayment(ORDER_NO, "paymentKey", 7000L);
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
            assertThatThrownBy(() -> orderPaymentService.confirmOrderPayment(ORDER_NO, "paymentKey", 7000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(PaymentErrorCode.PAYMENT_APPROVAL_FAIL);
        }
    }
}
