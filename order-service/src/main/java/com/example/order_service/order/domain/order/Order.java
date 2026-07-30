package com.example.order_service.order.domain.order;

import com.example.order_service.common.entity.BaseEntity;
import com.example.order_service.order.domain.order.context.CreateOrderContext;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.util.IdGenerator;

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

    @Builder(access = AccessLevel.PRIVATE)
    private Order (Long id, OrderStatus status, String orderName, Orderer orderer, ShippingAddress shippingAddress,
                   AppliedCartCoupon appliedCartCoupon, OrderAmount orderAmount, OrderCancelInfo orderCancelInfo) {

        Assert.notNull(id, "주문(Order) 생성시 아이디는 필수이다.");
        Assert.notNull(status, "주문(Order) 생성시 주문 상태는 필수이다.");
        Assert.hasText(orderName, "주문(Order) 생성시 주문 이름은 필수이다.");
        Assert.notNull(orderer, "주문(Order) 생성시 주문자는 필수이다.");
        Assert.notNull(shippingAddress, "주문(Order) 생성시 배송 정보는 필수이다.");
        Assert.notNull(orderAmount, "주문(Order) 생성시 주문 가격 정보는 필수이다.");

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
        return null;
    }

}
