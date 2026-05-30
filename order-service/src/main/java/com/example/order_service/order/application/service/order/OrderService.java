package com.example.order_service.order.application.service.order;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.api.dto.request.OrderSearchCondition;
import com.example.order_service.order.application.event.OrderCreatedEvent;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.command.OrderSearchCommand;
import com.example.order_service.order.application.service.order.dto.result.OrderDto;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import com.example.order_service.order.domain.repository.OrderRepository;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문을 저장한다
     * <p>
     * 주문 생성 Context에 맞는 주문을 생성하여 저장하고 커밋이 완료된 이후
     * 주문 생성 이벤트를 발행한다
     * </p>
     *
     * @param context 주문 생성 컨텍스트
     * @return 생성된 주문 DTO
     */
    public OrderDto.Detail saveOrder(OrderContext.CreateOrderContext context) {
        Order order = initialOrder(context);
        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishEvent(OrderCreatedEvent.from(savedOrder));
        return OrderDto.Detail.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto.Detail getOrder(String orderNo, Long userId) {
        Order order = orderRepository.findByOrderNoAndOrderer_UserId(orderNo, userId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderDto.Detail.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto.Summary> getOrders(Long userId, OrderSearchCommand command) {
        Page<Order> pageOrder = orderRepository.findByUserId(userId, command);
        return pageOrder.map(OrderDto.Summary::from);
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
        return OrderItem.create(itemContexts.productSnapshot(), itemContexts.itemPrice(), itemContexts.itemCoupon(),
                itemContexts.quantity(), itemContexts.options());
    }
}
