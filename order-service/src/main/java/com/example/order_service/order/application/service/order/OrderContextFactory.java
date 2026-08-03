package com.example.order_service.order.application.service.order;

import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.ordersheet.OrderSheet;
import org.springframework.stereotype.Component;

@Component
public class OrderContextFactory {

    public CreateOrderContext create(OrderSheet orderSheet) {
        return null;
    }
}
