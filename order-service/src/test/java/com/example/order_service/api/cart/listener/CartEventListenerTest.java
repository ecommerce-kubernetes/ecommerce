package com.example.order_service.api.cart.listener;

import com.example.order_service.api.cart.application.CartApplicationService;
import com.example.order_service.api.cart.listener.event.CartEventListener;
import com.example.order_service.api.order.application.event.OrderEventCode;
import com.example.order_service.api.order.application.event.OrderEventStatus;
import com.example.order_service.api.order.application.event.PaymentResultEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartEventListenerTest {
    @InjectMocks
    private CartEventListener cartEventListener;
    @Mock
    private CartApplicationService cartApplicationService;

    @Test
    @DisplayName("결제 완료시 장바구니에 주문한 상품을 삭제한다")
    void handlePaymentResult(){
        //given
        PaymentResultEvent event = PaymentResultEvent.builder()
                .orderId(1L)
                .userId(1L)
                .status(OrderEventStatus.SUCCESS)
                .code(OrderEventCode.PAYMENT_AUTHORIZED)
                .productVariantIds(List.of(1L, 2L))
                .failureReason(null)
                .build();
        //when
        cartEventListener.handlePaymentResult(event);
        //then
        verify(cartApplicationService, times(1)).cleanUpCartAfterOrder(1L, List.of(1L, 2L));
    }

    @Test
    @DisplayName("결제 실패시 장바구니 상품을 비우지 않는다")
    void handlePaymentResult_fail(){
        //given
        PaymentResultEvent event = PaymentResultEvent.builder()
                .orderId(1L)
                .userId(1L)
                .status(OrderEventStatus.FAILURE)
                .code(OrderEventCode.PAYMENT_AUTHORIZED_FAILED)
                .productVariantIds(List.of(1L, 2L))
                .failureReason("결제 승인 실패")
                .build();
        //when
        cartEventListener.handlePaymentResult(event);
        //then
        verify(cartApplicationService, never()).cleanUpCartAfterOrder(anyLong(), anyList());
    }


}
