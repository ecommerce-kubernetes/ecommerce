package com.example.order_service.order.application.service.order;

import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
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
    private final IdGenerator idGenerator;

    public Long saveOrder(CreateOrderContext context) {
        Order order = Order.create(context, idGenerator);
        Order save = orderRepository.save(order);
        return save.getId();
    }

    public void changePaid(String orderNo) {
    }

    public void changeCompleted(String orderNo) {
    }

    public void changeFailed(String orderNo, String reason) {
    }

}
