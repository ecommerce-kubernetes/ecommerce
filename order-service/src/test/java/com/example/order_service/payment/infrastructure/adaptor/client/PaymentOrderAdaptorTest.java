package com.example.order_service.payment.infrastructure.adaptor.client;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.exception.PortException;
import com.example.order_service.order.application.service.order.OrderQueryService;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderStatus;
import com.example.order_service.order.exception.OrderErrorCode;
import com.example.order_service.payment.application.port.dto.PaymentOrderResult;
import com.example.order_service.payment.application.port.dto.PaymentOrderStatus;
import com.example.order_service.payment.exception.PaymentOrderPortErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentOrderAdaptorTest {

    @InjectMocks
    private PaymentOrderAdaptor paymentOrderAdaptor;

    @Mock
    private OrderQueryService orderQueryService;

    @Test
    @DisplayName("주문을 조회한다.")
    void getOrder() {
        //given
        Long orderId = 1L;
        Long userId = 1L;
        OrderAmount orderAmount = OrderAmount.of(Money.wons(30000L), Money.wons(3000L), Money.wons(1000L),
                Money.wons(1000L), Money.wons(1000L), Money.wons(24000L));
        OrderResult orderResult = Instancio.of(OrderResult.class)
                .set(field("orderId"), orderId)
                .set(field("status"), OrderStatus.PENDING)
                .set(field("orderName"), "상품")
                .set(field("orderAmount"), orderAmount)
                .create();

        given(orderQueryService.getOrder(anyLong(), anyLong())).willReturn(orderResult);
        //when
        PaymentOrderResult result = paymentOrderAdaptor.getOrder(orderId, userId);
        //then
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.orderName()).isEqualTo(orderResult.orderName());
        assertThat(result.status()).isEqualTo(PaymentOrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문 조회시 주문을 찾을 수 없는 경우 예외가 발생한다.")
    void getOrder_client_error() {
        //given
        Long orderId = 1L;
        Long userId = 1L;
        given(orderQueryService.getOrder(anyLong(), anyLong()))
                .willThrow(new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        //when
        //then
        assertThatThrownBy(() -> paymentOrderAdaptor.getOrder(orderId, userId))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentOrderPortErrorCode.ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("주문 조회시 서버 오류가 발생하면 예외가 발생한다.")
    void getOrder_serverError() {
        //given
        Long orderId = 1L;
        Long userId = 1L;
        given(orderQueryService.getOrder(anyLong(), anyLong()))
                .willThrow(new RuntimeException("알 수 없는 오류"));
        //when
        //then
        assertThatThrownBy(() -> paymentOrderAdaptor.getOrder(orderId, userId))
                .isInstanceOf(PortException.class)
                .extracting("errorCode")
                .isEqualTo(PaymentOrderPortErrorCode.ORDER_SERVER_ERROR);
    }
}