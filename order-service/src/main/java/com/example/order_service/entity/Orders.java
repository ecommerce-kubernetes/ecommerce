package com.example.order_service.entity;

import com.example.order_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    @Setter
    private String status;
    private String deliveryAddress;
    private long originPrice;
    private long prodDiscount;
    private long couponDiscount;
    private long reserveDiscount;
    private long payment;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItems> orderItems = new ArrayList<>();


    public Orders(Long userId, String status, String deliveryAddress){
        this.userId = userId;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
    }

    public void setPaymentInfo(long originPrice, long prodDiscount, long couponDiscount, long reserveDiscount,
                               long payment){
        this.originPrice = originPrice;
        this.prodDiscount = prodDiscount;
        this.couponDiscount = couponDiscount;
        this.reserveDiscount = reserveDiscount;
        this.payment = payment;
    }

    public void addOrderItems(List<OrderItems> orderItems){
        for (OrderItems orderItem : orderItems) {
            addOrderItem(orderItem);
        }
    }

    public void addOrderItem(OrderItems orderItem){
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

}
