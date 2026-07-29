package com.example.order_service.order.application.service.order;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.result.OrderResultDeprecated;
import com.example.order_service.order.domain.order.Order;
import com.example.order_service.order.domain.order.OrderItem;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

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
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.paid();
    }

    public void changeCompleted(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.completed();
    }

    public void changeFailed(String orderNo, String reason) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.failed(reason);
    }

    private Order initialOrder(OrderContext.CreateOrderContext context) {
        String orderNo = orderNo();
        List<OrderItem> orderItems = context.orderItems().stream().map(this::createOrderItems).toList();
        return Order.init(orderNo, context.orderer(), context.shippingAddress(), context.cartCoupon(), orderItems,
                context.totalOriginalPrice(), context.totalProductDiscountAmount(), context.totalCouponDiscountAmount(),
                context.usedPoints(), context.totalPaymentAmount());
    }

    private String orderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + date + "-" + randomStr;
    }

    private OrderItem createOrderItems(OrderContext.ItemContext itemContexts) {
        return OrderItem.create(itemContexts.productSnapshot(), itemContexts.itemPrice(), itemContexts.itemCouponSnapshot(),
                itemContexts.quantity(), itemContexts.optionSnapshots());
    }
}
