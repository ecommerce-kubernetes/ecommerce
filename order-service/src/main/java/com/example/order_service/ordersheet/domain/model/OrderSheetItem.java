package com.example.order_service.ordersheet.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemOptionSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemPriceSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.OrderSheetItemProductSnapshot;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheetItem {
    private String sheetItemId;
    private OrderSheetItemProductSnapshot productSnapshot;
    private OrderSheetItemPriceSnapshot itemPrice;
    private Integer quantity;
    private List<OrderSheetItemOptionSnapshot> options;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheetItem(String sheetItemId, OrderSheetItemProductSnapshot productSnapshot, OrderSheetItemPriceSnapshot itemPrice, Integer quantity, List<OrderSheetItemOptionSnapshot> options) {
        this.sheetItemId = sheetItemId;
        this.productSnapshot = productSnapshot;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.options = options;
    }

    public static OrderSheetItem create(String sheetItemId, OrderSheetItemProductSnapshot productSnapshot, OrderSheetItemPriceSnapshot itemPrice,
                                        Integer quantity, List<OrderSheetItemOptionSnapshot> options) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidDomainValueException("OrderSheet 상품 주문 수량은 필수입니다");
        }
        return OrderSheetItem.reconstitute()
                .sheetItemId(sheetItemId)
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
