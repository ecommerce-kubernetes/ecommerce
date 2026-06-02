package com.example.order_service.order.application.service.order;

import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.model.Order;
import com.example.order_service.order.domain.model.OrderItem;
import com.example.order_service.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 주문 쓰기 담당 서비스
 * <p>
 * 주문 생성, 주문 상태 변경과 같은 주문 객체의 생성과 변경을 담당
 * </p>
 *
 * @author 최민식
 * @since 2026 06. 02
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderCommandService {
    private final OrderRepository orderRepository;

    /**
     * 주문을 저장한다
     * <p>
     * 주문 생성 Context에 맞는 주문을 생성하여 저장한다
     * </p>
     *
     * @param context 주문 생성 컨텍스트
     * @return 생성된 주문 DTO
     */
    public OrderResult.Create saveOrder(OrderContext.CreateOrderContext context) {
        Order order = initialOrder(context);
        Order savedOrder = orderRepository.save(order);
        return OrderResult.Create.from(savedOrder);
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
