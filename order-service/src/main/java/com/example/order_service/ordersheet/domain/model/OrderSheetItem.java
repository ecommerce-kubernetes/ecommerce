package com.example.order_service.ordersheet.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemProductSnapshot;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class OrderSheetItem {
    private OrderSheetItemProductSnapshot productSnapshot;
    private OrderSheetItemPriceSnapshot itemPrice;
    private Integer quantity;
    private List<OrderSheetItemOptionSnapshot> options;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItem(OrderSheetItemProductSnapshot productSnapshot, OrderSheetItemPriceSnapshot itemPrice, Integer quantity, List<OrderSheetItemOptionSnapshot> options) {
        this.productSnapshot = productSnapshot;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.options = options;
    }

    public static OrderSheetItem create(OrderSheetItemProductSnapshot productSnapshot, OrderSheetItemPriceSnapshot itemPrice,
                                        Integer quantity, List<OrderSheetItemOptionSnapshot> options) {
        if (quantity == null || quantity <= 0) {
            //TODO 커스텀 예외
            throw new RuntimeException();
        }
        return OrderSheetItem.reconstitute()
                .productSnapshot(productSnapshot)
                .itemPrice(itemPrice)
                .quantity(quantity)
                .options(options)
                .build();
    }

    public Money getLineTotal() {
        return itemPrice.getDiscountedPrice().multiple(quantity);
    }

    public Money getDiscountLineTotal() {
        return itemPrice.getDiscountAmount().multiple(quantity);
    }

    public Money getOriginalLineTotal() {
        return itemPrice.getOriginalPrice().multiple(quantity);
    }
}
