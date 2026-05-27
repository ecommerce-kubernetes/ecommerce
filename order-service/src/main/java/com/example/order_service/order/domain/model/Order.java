package com.example.order_service.order.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
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
    private String orderNo;
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
    private OrderCouponSnapshot cartCoupon;
    private Money totalOriginalPrice;
    private Money totalProductDiscountAmount;
    private Money totalCouponDiscountAmount;
    private Money usedPoints;
    private Money totalPaymentAmount;
    @Enumerated(EnumType.STRING)
    private OrderFailureCode failureCode;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(String orderNo, OrderStatus status, String orderName, Orderer orderer, ShippingAddress shippingAddress,
                  OrderCouponSnapshot cartCoupon, Money totalOriginalPrice, Money totalProductDiscountAmount, Money totalCouponDiscountAmount,
                  Money usedPoints, Money totalPaymentAmount, OrderFailureCode failureCode) {
        this.orderNo = orderNo;
        this.status = status;
        this.orderName = orderName;
        this.orderer = orderer;
        this.shippingAddress = shippingAddress;
        this.cartCoupon = cartCoupon;
        this.totalOriginalPrice = totalOriginalPrice;
        this.totalProductDiscountAmount = totalProductDiscountAmount;
        this.totalCouponDiscountAmount = totalCouponDiscountAmount;
        this.usedPoints = usedPoints;
        this.totalPaymentAmount = totalPaymentAmount;
        this.failureCode = failureCode;
    }

    public static Order init(String orderNo, Orderer orderer, ShippingAddress shippingAddress, OrderCouponSnapshot cartCoupon,
                             List<OrderItem> orderItems, Money totalOriginalPrice, Money totalProductDiscountAmount,
                             Money totalCouponDiscountAmount, Money usedPoints, Money totalPaymentAmount) {
        String orderName = generateOrderName(orderItems);
        Order order = create(orderNo, OrderStatus.PENDING, orderName, orderer, shippingAddress, cartCoupon, totalOriginalPrice,
                totalProductDiscountAmount, totalCouponDiscountAmount, usedPoints, totalPaymentAmount, null);
        for(OrderItem orderItem: orderItems) {
            order.addItem(orderItem);
        }
        return order;
    }

    private static Order create(String orderNo, OrderStatus orderStatus, String orderName, Orderer orderer, ShippingAddress shippingAddress,
                                OrderCouponSnapshot cartCoupon, Money totalOriginalPrice, Money totalProductDiscountAmount,
                                Money totalCouponDiscountAmount, Money usedPoints, Money totalPaymentAmount, OrderFailureCode code) {
        return Order.builder()
                .orderNo(orderNo)
                .status(orderStatus)
                .orderName(orderName)
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .cartCoupon(cartCoupon)
                .totalOriginalPrice(totalOriginalPrice)
                .totalProductDiscountAmount(totalProductDiscountAmount)
                .totalCouponDiscountAmount(totalCouponDiscountAmount)
                .usedPoints(usedPoints)
                .totalPaymentAmount(totalPaymentAmount)
                .failureCode(code)
                .build();
    }

    private static String generateOrderName(List<OrderItem> items) {
        String firstProdName = items.getFirst().getProduct().getProductName();
        int size = items.size();
        if (size == 1) {
            return firstProdName;
        }
        return firstProdName + " 외 " + (size - 1) + "건";
    }

    private void addItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
