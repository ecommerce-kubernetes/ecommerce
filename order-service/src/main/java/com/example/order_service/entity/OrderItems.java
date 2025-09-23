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
    private Long originPrice;
    private Long discountRate;
    private Long discountedPrice;
    private Long lineTotal;

    private int quantity;
    private String thumbnail;

    public OrderItems(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public void setProductData(Long productId, String productName, String optionJson, long originPrice,
                               long discountRate, long discountedPrice, long lineTotal){
        this.productId = productId;
        this.productName = productName;
        this.optionJson = optionJson;
        this.originPrice = originPrice;
        this.discountRate = discountRate;
        this.discountedPrice = discountedPrice;
        this.lineTotal = lineTotal;
    }

    protected void setOrder(Orders order){
        this.order = order;
    }
}
