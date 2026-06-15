package com.example.order_service.cart.listener;

import com.example.order_service.cart.application.CartAppService;
import com.example.order_service.cart.listener.event.CartEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CartEventListenerTest {
    @InjectMocks
    private CartEventListener cartEventListener;
    @Mock
    private CartAppService cartAppService;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

    @Test
    @DisplayName("결제가 성공한 상품을 장바구니에서 삭제한다")
    void handlePaymentCompletedEvent(){
    }

}
