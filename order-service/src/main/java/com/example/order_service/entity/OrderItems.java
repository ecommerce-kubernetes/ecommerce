package com.example.order_service.entity;

import com.example.order_service.dto.request.OrderItemRequest;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.entity.base.BaseEntity;
import com.example.order_service.entity.convert.ItemOptionsConverter;
import com.example.order_service.service.client.dto.ProductResponse;
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

    @Column(columnDefinition = "TEXT")
    @Convert(converter = ItemOptionsConverter.class)
    private List<ItemOptionResponse> option;
    private Long unitPrice;
    private Integer discountRate;
    private Long discountedPrice;
    private Long lineTotal;

    private int quantity;
    private String thumbnail;

    public OrderItems(Long productId, Long productVariantId, String productName,
                      List<ItemOptionResponse> option, Long unitPrice, Integer discountRate, Long discountedPrice,
                      Long lineTotal, int quantity, String thumbnail){
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.option = option;
        this.unitPrice = unitPrice;
        this.discountRate = discountRate;
        this.discountedPrice = discountedPrice;
        this.lineTotal = lineTotal;
        this.quantity = quantity;
        this.thumbnail = thumbnail;
    }

    public static OrderItems from(OrderItemRequest orderItemRequest, Map<Long, ProductResponse> productResponseMap){
        ProductResponse productResponse = productResponseMap.get(orderItemRequest.getProductVariantId());
        return new OrderItems(productResponse.getProductId(),
                productResponse.getProductVariantId(),
                productResponse.getProductName(),
                productResponse.getItemOptions(),
                productResponse.getProductPrice().getUnitPrice(),
                productResponse.getProductPrice().getDiscountRate(),
                productResponse.getProductPrice().getDiscountedPrice(),
                productResponse.getProductPrice().getDiscountedPrice() * orderItemRequest.getQuantity(),
                orderItemRequest.getQuantity(),
                productResponse.getThumbnailUrl());
    }

    public OrderItems(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public void setProductData(Long productId, String productName, String optionJson, long originPrice,
                               long discountRate, long discountedPrice, long lineTotal){
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = originPrice;
//        this.discountRate = discountRate;
        this.discountedPrice = discountedPrice;
        this.lineTotal = lineTotal;
    }

    protected void setOrder(Orders order){
        this.order = order;
    }
}
