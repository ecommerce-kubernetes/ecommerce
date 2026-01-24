package com.example.order_service.api.order.domain.service.dto.result;

import com.example.order_service.api.order.domain.model.OrderItem;
import com.example.order_service.api.order.domain.model.vo.OrderItemPrice;
import com.example.order_service.api.order.domain.model.vo.OrderedProduct;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderItemDto {
    private Long id;
    private OrderedProduct orderedProduct;
    private OrderItemPrice orderItemPrice;
    private int quantity;
    private Long lineTotal;
    private List<ItemOptionDto> itemOptions;

    public static OrderItemDto from(OrderItem orderItem) {
        List<ItemOptionDto> itemOptions = orderItem.getItemOptions().stream().map(ItemOptionDto::from).toList();
        return OrderItemDto.builder()
                .id(orderItem.getId())
                .orderedProduct(orderItem.getOrderedProduct())
                .orderItemPrice(orderItem.getOrderItemPrice())
                .quantity(orderItem.getQuantity())
                .lineTotal(orderItem.getLineTotal())
                .itemOptions(itemOptions)
                .build();
    }
}
