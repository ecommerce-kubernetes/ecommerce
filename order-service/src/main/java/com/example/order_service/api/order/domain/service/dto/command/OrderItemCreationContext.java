package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.domain.model.vo.OrderItemPrice;
import com.example.order_service.api.order.domain.model.vo.OrderedProduct;
import com.example.order_service.api.order.domain.model.vo.Orderer;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderItemCreationContext {
    private OrderedProduct orderedProduct;
    private OrderItemPrice orderItemPrice;
    private int quantity;
    private Long lineTotal;
    private List<ItemOption> itemOptions;

    @Builder
    @Getter
    public static class ItemOption {
        private String optionTypeName;
        private String optionValueName;

        public static ItemOption of(String optionTypeName, String optionValueName) {
            return ItemOption.builder()
                    .optionTypeName(optionTypeName)
                    .optionValueName(optionValueName)
                    .build();
        }
    }

    public static OrderItemCreationContext of(OrderedProduct orderedProduct, OrderItemPrice orderItemPrice, int quantity, long lineTotal, List<ItemOption> itemOptions) {
        return OrderItemCreationContext.builder()
                .orderedProduct(orderedProduct)
                .orderItemPrice(orderItemPrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .itemOptions(itemOptions)
                .build();
    }
}
