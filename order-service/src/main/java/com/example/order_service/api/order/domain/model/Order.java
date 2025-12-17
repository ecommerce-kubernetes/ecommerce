package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.entity.BaseEntity;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String orderName;
    private String deliveryAddress;
    private Long totalOriginPrice;
    private Long totalProductDiscount;
    private Long couponDiscount;
    private Long pointDiscount;
    private Long finalPaymentAmount;

    @Enumerated(EnumType.STRING)
    private OrderFailureCode failureCode;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Coupon coupon;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(Long userId, OrderStatus status, String orderName, String deliveryAddress, Long totalOriginPrice,
                 Long totalProductDiscount, Long couponDiscount, Long pointDiscount, Long finalPaymentAmount, OrderFailureCode failureCode) {
        this.userId = userId;
        this.status = status;
        this.orderName = orderName;
        this.deliveryAddress = deliveryAddress;
        this.totalOriginPrice = totalOriginPrice;
        this.totalProductDiscount = totalProductDiscount;
        this.couponDiscount = couponDiscount;
        this.pointDiscount = pointDiscount;
        this.finalPaymentAmount = finalPaymentAmount;
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

    private static String generateOrderName(List<OrderItemSpec> itemSpecs){
        if (itemSpecs.isEmpty()){
            throw new OrderVerificationException("주문 상품은 1개 이상이여야 합니다");
        }

        String firstProductName = itemSpecs.get(0).getProductName();
        int size = itemSpecs.size();

        if(size == 1){
            return firstProductName;
        } else {
            return firstProductName + " 외 " + (size - 1) + "건";
        }
    }

    public static Order create(OrderCreationContext context) {
        String generatedOrderName = generateOrderName(context.getItemSpecs());
        Order order = createOrder(context, generatedOrderName);
        for (OrderItemSpec item : context.getItemSpecs()) {
            order.addOrderItem(OrderItem.create(item));
        }
        if (context.getPriceResult().getAppliedCoupon() != null) {
            order.addCoupon(Coupon.create(context.getPriceResult().getAppliedCoupon()));
        }

        return order;
    }

    private static Order createOrder(OrderCreationContext context, String orderName){
        return Order.builder()
                .userId(context.getUserId())
                .status(OrderStatus.PENDING)
                .orderName(orderName)
                .deliveryAddress(context.getDeliveryAddress())
                .totalOriginPrice(context.getPriceResult().getPaymentInfo().getTotalOriginPrice())
                .totalProductDiscount(context.getPriceResult().getPaymentInfo().getTotalProductDiscount())
                .couponDiscount(context.getPriceResult().getPaymentInfo().getCouponDiscount())
                .pointDiscount(context.getPriceResult().getPaymentInfo().getUsedPoint())
                .finalPaymentAmount(context.getPriceResult().getPaymentInfo().getFinalPaymentAmount())
                .failureCode(null)
                .build();
    }

}
