package com.example.order_service.api.order.domain.model;

import com.example.order_service.dto.OrderCalculationResult;
import com.example.order_service.api.order.controller.dto.request.CreateOrderRequest;
import com.example.order_service.api.common.entity.BaseEntity;
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
    private Long usedCouponId;
    private Long originPrice;
    private Long prodDiscount;
    private Long couponDiscount;
    private Long pointDiscount;
    private Long amountToPay;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItems> orderItems = new ArrayList<>();


    public Orders(Long userId, String status, String deliveryAddress){
        this.userId = userId;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
    }

    public static Orders create(Long userId, CreateOrderRequest createOrderRequest, OrderCalculationResult result){
        Orders orders = new Orders(userId, createOrderRequest.getCouponId(), "PENDING", createOrderRequest.getDeliveryAddress(),
                result.getOriginOrderItemPrice(),
                result.getProductDiscountAmount(),
                result.getCouponDiscount(),
                createOrderRequest.getPointToUse(),
                result.getAmountToPay());
        List<OrderItems> orderItems = createOrderRequest.getItems().stream()
                .map(item -> OrderItems.from(item, result.getProductByVariantId()))
                .toList();
        orders.addOrderItems(orderItems);
        return orders;
    }

    public Orders(Long userId, Long usedCouponId, String status, String deliveryAddress, Long originPrice,
                  Long prodDiscount, Long couponDiscount, Long pointDiscount, Long amountToPay){
        this.userId = userId;
        this.usedCouponId = usedCouponId;
        this.status = status;
        this.deliveryAddress = deliveryAddress;
        this.originPrice = originPrice;
        this.prodDiscount = prodDiscount;
        this.couponDiscount = couponDiscount;
        this.pointDiscount = pointDiscount;
        this.amountToPay = amountToPay;
    }

    public void cancel(){
        this.status = "CANCEL";
    }

    public void complete(){
        this.status = "COMPLETE";
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
