package com.example.order_service.entity;

import com.example.order_service.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrderItems extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    private Long productId;
    private Long productVariantId;
    private String productName;
    private String optionJson;
    private int price;
    private int quantity;
    private String mainImgUrl;

    public OrderItems(Orders order, Long productId, String productName, int price, int quantity, String mainImgUrl){
        this.order = order;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.mainImgUrl = mainImgUrl;
        order.getOrderItems().add(this);
    }

    public OrderItems(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    protected void setOrder(Orders order){
        this.order = order;
    }
}
