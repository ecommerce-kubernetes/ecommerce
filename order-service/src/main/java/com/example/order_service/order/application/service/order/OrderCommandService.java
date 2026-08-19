package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.application.port.OrderRepository;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderCancelInfo;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderCommandService {
    private final OrderRepository orderRepository;
    private final IdGenerator idGenerator;

    public Long saveOrder(CreateOrderContext context) {
        Order order = Order.create(context, idGenerator);

        if(order.getOrderAmount().getTotalPaymentAmount().equals(Money.ZERO)){
            order.accept();
        }

        Order save = orderRepository.save(order);
        return save.getId();
    }

    public void changeAccepted(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.accept();
        orderRepository.save(order);
    }

    public void changeCompleted(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.complete();
    }

    public void changeFailed(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        OrderCancelInfo failedInfo = OrderCancelInfo.of(reason, LocalDateTime.now());
        order.failed(failedInfo);
        orderRepository.save(order);
    }

}
