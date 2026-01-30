package com.example.order_service.api.cart.listener;

import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.cart.listener.event.CartEventListener;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CartEventListenerTest {
    @InjectMocks
    private CartEventListener cartEventListener;
    @Mock
    private CartFacade cartFacade;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

}
