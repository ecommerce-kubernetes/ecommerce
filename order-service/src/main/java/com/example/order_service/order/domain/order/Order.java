package com.example.order_service.order.domain.order;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import jakarta.persistence.*;
import lombok.AccessLevel;
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
}
