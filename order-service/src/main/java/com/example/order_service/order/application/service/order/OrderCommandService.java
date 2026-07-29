package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.result.OrderResultDeprecated;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderCommandService {
    private final OrderRepository orderRepository;


    public OrderResultDeprecated.Create saveOrder(OrderContext.CreateOrderContext context) {
        Order order = initialOrder(context);
        Order savedOrder = orderRepository.save(order);
        return OrderResultDeprecated.Create.from(savedOrder);
    }

    public void changePaid(String orderNo) {
    }

    public void changeCompleted(String orderNo) {
    }

    public void changeFailed(String orderNo, String reason) {
    }

    private Order initialOrder(OrderContext.CreateOrderContext context) {
        return null;
    }
}
