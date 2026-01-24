package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.order.domain.service.dto.command.OrderItemCreationContext;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ItemOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String optionTypeName;
    private String optionValueName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Builder(access = AccessLevel.PRIVATE)
    private ItemOption(String optionTypeName, String optionValueName){
        this.optionTypeName = optionTypeName;
        this.optionValueName = optionValueName;
    }

    protected void setOrderItem(OrderItem orderItem){
        this.orderItem = orderItem;
    }

    public static ItemOption create(OrderItemCreationContext.ItemOption itemOption) {
        return ItemOption.builder()
                .optionTypeName(itemOption.getOptionTypeName())
                .optionValueName(itemOption.getOptionValueName())
                .build();
    }
}
