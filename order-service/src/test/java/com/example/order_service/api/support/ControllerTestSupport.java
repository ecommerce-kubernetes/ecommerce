package com.example.order_service.api.support;

import com.example.order_service.api.cart.facade.CartFacade;
import com.example.order_service.api.cart.controller.CartController;
import com.example.order_service.api.notification.controller.NotificationController;
import com.example.order_service.api.notification.service.NotificationService;
import com.example.order_service.api.order.facade.OrderFacade;
import com.example.order_service.api.order.controller.OrderController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CartController.class, OrderController.class, NotificationController.class, DummyController.class})
public abstract class ControllerTestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected CartFacade cartFacade;
    @MockitoBean
    protected OrderFacade orderFacade;
    @MockitoBean
    protected NotificationService notificationService;
}
