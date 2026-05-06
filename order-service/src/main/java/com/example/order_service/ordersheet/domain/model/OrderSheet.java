package com.example.order_service.ordersheet.domain.model;

import com.example.order_service.common.domain.vo.Money;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderSheet {
    private String sheetId;
    private List<OrderSheetItem> items;
    private Money totalOriginalPrice;
    private Money totalProductDiscountAmount;
    private Money totalPaymentAmount;
    private LocalDateTime expiresAt;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheet(String sheetId, List<OrderSheetItem> items, LocalDateTime expiresAt) {
        this.sheetId = sheetId;
        this.items = items;
        this.expiresAt = expiresAt.plusMinutes(30);
        this.totalOriginalPrice = calcTotalOriginalPrice(items);
        this.totalProductDiscountAmount = calcTotalProductDiscountAmount(items);
        this.totalPaymentAmount = calcTotalPaymentAmount(items);
    }

    public static OrderSheet create(String sheetId, List<OrderSheetItem> items, LocalDateTime createdAt) {
        if (items == null || items.isEmpty()) {
            //TODO 커스텀 예외 반환
            throw new RuntimeException();
        }
        return new OrderSheet(sheetId, items, createdAt);
    }

    private Money calcTotalOriginalPrice(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getOriginalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private Money calcTotalProductDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getDiscountLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private Money calcTotalPaymentAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getLineTotal)
                .reduce(Money.ZERO, Money::add);
    }
}
