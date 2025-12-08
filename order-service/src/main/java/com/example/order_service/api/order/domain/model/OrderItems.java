package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.order.controller.dto.request.CreateOrderItemRequest;
import com.example.order_service.api.common.entity.BaseEntity;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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

//    @Column(columnDefinition = "TEXT")
//    @Convert(converter = ItemOptionsConverter.class)
//    private List<ItemOption> itemOption;
    private Long unitPrice;
    private Integer discountRate;
    private Long discountedPrice;
    private Long lineTotal;

    private int quantity;
    private String thumbnail;

    public OrderItems(Long productId, Long productVariantId, String productName,
                      List<CartProductResponse.ItemOption> itemOption, Long unitPrice, Integer discountRate, Long discountedPrice,
                      Long lineTotal, int quantity, String thumbnail){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
//        this.itemOption = itemOption;
        this.unitPrice = unitPrice;
        this.discountRate = discountRate;
        this.discountedPrice = discountedPrice;
        this.lineTotal = lineTotal;
        this.quantity = quantity;
        this.thumbnail = thumbnail;
    }

    public static OrderItems from(CreateOrderItemRequest createOrderItemRequest, Map<Long, CartProductResponse> productResponseMap){
        CartProductResponse cartProductResponse = productResponseMap.get(createOrderItemRequest.getProductVariantId());
        return new OrderItems(cartProductResponse.getProductId(),
                cartProductResponse.getProductVariantId(),
                cartProductResponse.getProductName(),
                cartProductResponse.getItemOptions(),
                cartProductResponse.getUnitPrice().getOriginalPrice(),
                cartProductResponse.getUnitPrice().getDiscountRate(),
                cartProductResponse.getUnitPrice().getDiscountedPrice(),
                cartProductResponse.getUnitPrice().getDiscountedPrice() * createOrderItemRequest.getQuantity(),
                createOrderItemRequest.getQuantity(),
                cartProductResponse.getThumbnailUrl());
    }

    protected void setOrder(Orders order){
        this.order = order;
    }
}
