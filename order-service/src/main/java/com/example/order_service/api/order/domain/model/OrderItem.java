package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.common.entity.BaseEntity;
import com.example.order_service.api.order.domain.model.vo.OrderItemPrice;
import com.example.order_service.api.order.domain.model.vo.OrderedProduct;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
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

    @Embedded
    private OrderedProduct orderedProduct;
    @Embedded
    private OrderItemPrice orderItemPrice;
    private Long lineTotal;
    private Integer quantity;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ItemOption> itemOptions = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItem(OrderedProduct orderedProduct, OrderItemPrice orderItemPrice, Long lineTotal, Integer quantity){
        this.orderedProduct = orderedProduct;
        this.orderItemPrice = orderItemPrice;
        this.lineTotal = lineTotal;
        this.quantity = quantity;
    }

    protected void setOrder(Order order){
        this.order = order;
    }

    public void addItemOption(ItemOption itemOption){
        this.itemOptions.add(itemOption);
        itemOption.setOrderItem(this);
    }

    public static OrderItem create (OrderItemCreationContext itemContext) {
        OrderItem orderItem = of(itemContext);
        if (itemContext.getItemOptions() != null && !itemContext.getItemOptions().isEmpty()) {
            for (OrderItemCreationContext.ItemOption option : itemContext.getItemOptions()) {
                orderItem.addItemOption(ItemOption.create(option));
            }
        }
        return orderItem;
    }

    public static OrderItem of(OrderItemCreationContext itemContext){
        return OrderItem.builder()
                .orderedProduct(itemContext.getOrderedProduct())
                .orderItemPrice(itemContext.getOrderItemPrice())
                .lineTotal(itemContext.getLineTotal())
                .quantity(itemContext.getQuantity())
                .build();
    }
}

