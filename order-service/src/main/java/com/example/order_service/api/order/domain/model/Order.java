package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.entity.BaseEntity;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.model.vo.OrderPriceInfo;
import com.example.order_service.api.order.domain.service.dto.command.CreateOrderCommand;
import com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderNo;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String orderName;
    private String deliveryAddress;
    @Embedded
    private OrderPriceInfo priceInfo;

    @Enumerated(EnumType.STRING)
    private OrderFailureCode failureCode;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Coupon coupon;

    @OneToOne(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Payment payment;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNo, Long userId, OrderStatus status, String orderName, String deliveryAddress, OrderPriceInfo priceInfo, OrderFailureCode failureCode) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.status = status;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.priceInfo = priceInfo;
        this.failureCode = failureCode;
    }

    private void addOrderItem(OrderItem orderItem){
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    private void addCoupon(Coupon coupon){
        this.coupon = coupon;
        coupon.setOrder(this);
    }

    public void addPayment(Payment payment) {
        this.payment = payment;
        payment.setOrder(this);
    }

    public void changeStatus(OrderStatus orderStatus){
        this.status = orderStatus;
    }

    public boolean isOwner(Long accessUserId) {
        return this.userId.equals(accessUserId);
    }

    public void canceled(OrderFailureCode code) {
        this.status = OrderStatus.CANCELED;
        this.failureCode = code;
    }

    public static Order create(CreateOrderCommand context) {
        validateOrderItems(context.getItemCommands());
        String orderNo = generatedOrderNo();
        String generatedOrderName = generateOrderName(context.getItemCommands());
        Order order = createOrder(context, orderNo, generatedOrderName);
        for (CreateOrderItemCommand item : context.getItemCommands()) {
            order.addOrderItem(OrderItem.create(item));
        }
        if (context.getAppliedCoupon() != null) {
            order.addCoupon(Coupon.create(context.getAppliedCoupon()));
        }

        return order;
    }

    private static void validateOrderItems(List<CreateOrderItemCommand> itemSpecs) {
        if (itemSpecs == null || itemSpecs.isEmpty()) {
            throw new BusinessException(OrderErrorCode.ORDER_ITEM_MINIMUM_ONE_REQUIRED);
        }
    }

    private static String generateOrderName(List<CreateOrderItemCommand> itemSpecs){
        String firstProductName = itemSpecs.get(0).getProductName();
        int size = itemSpecs.size();

        if(size == 1){
            return firstProductName;
        } else {
            return firstProductName + " 외 " + (size - 1) + "건";
        }
    }

    private static String generatedOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + date + "-" + randomStr;
    }

    private static Order createOrder(CreateOrderCommand context, String orderNo, String orderName){
        return Order.builder()
                .orderNo(orderNo)
                .userId(context.getUserId())
                .status(OrderStatus.PENDING)
                .orderName(orderName)
                .deliveryAddress(context.getDeliveryAddress())
                .priceInfo(context.getOrderPriceInfo())
                .failureCode(null)
                .build();
    }
}
