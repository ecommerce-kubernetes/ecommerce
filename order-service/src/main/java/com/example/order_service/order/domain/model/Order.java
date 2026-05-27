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
    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<Payment> payments = new ArrayList<>();

    @Builder(access =AccessLevel.PRIVATE)
    private Order (String orderNo, OrderStatus status, String orderName, Orderer orderer, ShippingAddress shippingAddress,
                   OrderCouponSnapshot cartCoupon, Money totalOriginalPrice, Money totalProductDiscountAmount, Money totalCouponDiscountAmount,
                   Money usedPoints, Money totalPaymentAmount, OrderFailureCode failureCode){
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
}
