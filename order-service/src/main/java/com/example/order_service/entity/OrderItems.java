package com.example.order_service.entity;

import com.example.order_service.entity.base.BaseEntity;
import com.example.order_service.service.dto.SuccessOrderItemDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

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
    private long originPrice;
    private long discountRate;
    private long discountedPrice;
    private long finalPrice;

    private int quantity;
    private String thumbnail;

    public OrderItems(Long productVariantId, int quantity){
        this.productVariantId = productVariantId;
        this.quantity = quantity;
    }

    public void setOrderItemInfo(List<SuccessOrderItemDto> itemsData){
        SuccessOrderItemDto itemDto = itemsData.stream().filter(item -> item.getProductVariantId().equals(this.productVariantId))
                .findFirst().get();

        this.productId = itemDto.getProductId();
        this.productName = itemDto.getProductName();
        this.optionJson = itemDto.getOptionJson();
        this.originPrice = itemDto.getOriginPrice();
        this.discountRate = itemDto.getDiscountRate();
        this.discountedPrice = itemDto.getDiscountedPrice();
        this.finalPrice = itemDto.getFinalPrice();
        this.thumbnail = itemDto.getThumbnail();
    }

    protected void setOrder(Orders order){
        this.order = order;
    }
}
