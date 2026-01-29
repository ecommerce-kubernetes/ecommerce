package com.example.order_service.api.cart.listener;

import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.cart.listener.event.CartEventListener;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.facade.event.OrderEventStatus;
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
    private CartFacade cartFacade;
    public static final String ORDER_NO = "ORD-20260101-AB12FVC";

}
