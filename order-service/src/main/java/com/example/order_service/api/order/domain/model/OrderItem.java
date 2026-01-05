package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.entity.BaseEntity;
import com.example.order_service.api.order.domain.service.dto.command.CreateOrderItemCommand;
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
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long productId;
    private Long productVariantId;
    private String productName;
    private Long originPrice;
    private Integer discountRate;
    private Long discountAmount;
    private Long discountedPrice;
    private Long lineTotal;
    private Integer quantity;
    private String thumbnail;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ItemOption> itemOptions = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItem(Order order, Long productId, Long productVariantId, String productName, Long originPrice,
                     Integer discountRate, Long discountAmount, Long discountedPrice, Long lineTotal, Integer quantity, String thumbnail) {
        this.order = order;
        this.productId = productId;
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.originPrice = originPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice = discountedPrice;
        this.lineTotal = lineTotal;
        this.quantity = quantity;
        this.thumbnail = thumbnail;
    }

    protected void setOrder(Order order){
        this.order = order;
    }

    public void addItemOption(ItemOption itemOption){
        this.itemOptions.add(itemOption);
        itemOption.setOrderItem(this);
    }

    public static OrderItem create(CreateOrderItemCommand createOrderItemCommand){
        OrderItem orderItem = of(createOrderItemCommand);
        if(createOrderItemCommand.getItemOptions() != null && !createOrderItemCommand.getItemOptions().isEmpty()) {
            for (CreateOrderItemCommand.ItemOption itemOption : createOrderItemCommand.getItemOptions()) {
                orderItem.addItemOption(ItemOption.create(itemOption));
            }
        }

        return orderItem;
    }

    public static OrderItem of(CreateOrderItemCommand createOrderItemCommand){
        return OrderItem.builder()
                .productId(createOrderItemCommand.getProductId())
                .productVariantId(createOrderItemCommand.getProductVariantId())
                .productName(createOrderItemCommand.getProductName())
                .originPrice(createOrderItemCommand.getUnitPrice().getOriginalPrice())
                .discountRate(createOrderItemCommand.getUnitPrice().getDiscountRate())
                .discountAmount(createOrderItemCommand.getUnitPrice().getDiscountAmount())
                .discountedPrice(createOrderItemCommand.getUnitPrice().getDiscountedPrice())
                .lineTotal(createOrderItemCommand.getLineTotal())
                .quantity(createOrderItemCommand.getQuantity())
                .thumbnail(createOrderItemCommand.getThumbnailUrl())
                .build();
    }
}
