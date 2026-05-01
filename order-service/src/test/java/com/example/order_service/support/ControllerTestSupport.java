package com.example.order_service.support;

import com.example.order_service.api.support.DummyController;
import com.example.order_service.api.support.fixture.FixtureMonkeyFactory;
import com.example.order_service.cart.api.CartController;
import com.example.order_service.cart.application.CartAppService;
import com.example.order_service.notification.controller.NotificationController;
import com.example.order_service.notification.service.NotificationService;
import com.example.order_service.order.api.OrderController;
import com.example.order_service.order.application.OrderAppService;
import com.example.order_service.ordersheet.api.OrderSheetController;
import com.example.order_service.ordersheet.application.OrderSheetAppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CartController.class, OrderController.class, NotificationController.class, DummyController.class, OrderSheetController.class})
public abstract class ControllerTestSupport {
    protected final FixtureMonkey fixtureMonkey = FixtureMonkeyFactory.get;
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected CartAppService cartAppService;
    @MockitoBean
    protected OrderAppService orderAppService;
    @MockitoBean
    protected NotificationService notificationService;
    @MockitoBean
    protected OrderSheetAppService orderSheetAppService;
}
