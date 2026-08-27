package com.example.order_service.order.domain.order;

import com.example.order_service.common.entity.BaseAggregateRoot;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.common.util.IdGenerator;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.order.context.CreateOrderItemContext;
import com.example.order_service.order.domain.order.event.OrderAcceptedEvent;
import com.example.order_service.order.domain.order.event.OrderFailedEvent;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.example.order_service.order.exception.OrderErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
public class Order extends BaseAggregateRoot {

    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String orderName;

    @Embedded
    private Orderer orderer;

    @Embedded
    private ShippingAddress shippingAddress;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Embedded
    private AppliedCartCoupon appliedCartCoupon;

    @Embedded
    private OrderAmount orderAmount;

    @Embedded
    private OrderCancelInfo orderCancelInfo;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(Long id, OrderStatus status, String orderName, Orderer orderer, ShippingAddress shippingAddress,
                  AppliedCartCoupon appliedCartCoupon, OrderAmount orderAmount, OrderCancelInfo orderCancelInfo) {
        this.id = id;
        this.status = status;
        this.orderName = orderName;
        this.orderer = orderer;
        this.shippingAddress = shippingAddress;
        this.appliedCartCoupon = appliedCartCoupon;
        this.orderAmount = orderAmount;
        this.orderCancelInfo = orderCancelInfo;
    }

    public static Order create(CreateOrderContext context, IdGenerator idGenerator) {
        Assert.notNull(idGenerator, "주문(Order) 생성시 아이디 생성기는 필수이다.");

        Long id = idGenerator.generate();
        Assert.notNull(id, "주문(Order) 생성시 아이디는 필수이다.");

        if (context.items().isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEMS_REQUIRED);
        }

        String orderName = generateOrderName(context.items());

        Order order = Order.builder()
                .id(id)
                .status(OrderStatus.PENDING)
                .orderName(orderName)
                .orderer(context.orderer())
                .shippingAddress(context.shippingAddress())
                .appliedCartCoupon(context.appliedCartCoupon())
                .orderAmount(context.orderAmount())
                .orderCancelInfo(null)
                .build();

        for (CreateOrderItemContext itemCtx : context.items()) {
            OrderItem orderItem = OrderItem.create(itemCtx, idGenerator);
            order.addOrderItem(orderItem);
        }

        return order;
    }

    private static String generateOrderName(List<CreateOrderItemContext> items) {
        int size = items.size();
        String firstProductName = items.getFirst().productSnapshot().getProductName();
        if (size == 1) {
            return firstProductName;
        }

        return String.format("%s 외 %d건", firstProductName, size - 1);
    }

    private void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void accept() {
        if (!this.status.equals(OrderStatus.PENDING)) {
            throw new BusinessException(OrderErrorCode.ORDER_CANNOT_PAID);
        }
        this.status = OrderStatus.ACCEPTED;
        registerAcceptedEvent();
    }

    public void complete() {
        if (!this.status.equals(OrderStatus.ACCEPTED)) {
            throw new BusinessException(OrderErrorCode.ORDER_CANNOT_COMPLETED);
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void failed(OrderCancelInfo failedInfo) {
        if (!this.status.equals(OrderStatus.PENDING) && !this.status.equals(OrderStatus.ACCEPTED)) {
            throw new BusinessException(OrderErrorCode.ORDER_CANNOT_FAILED);
        }
        this.status = OrderStatus.FAILED;
        this.orderCancelInfo = failedInfo;
        registerFailedEvent();
    }

    private void registerAcceptedEvent() {
        OrderAcceptedEvent event = OrderAcceptedEvent.from(this);
        registerEvent(event);
    }

    private void registerFailedEvent() {
        OrderFailedEvent event = OrderFailedEvent.from(this);
        registerEvent(event);
    }
}
