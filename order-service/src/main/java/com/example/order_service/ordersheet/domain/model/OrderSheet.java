package com.example.order_service.ordersheet.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheet {
    private String sheetId;
    private List<OrderSheetItem> items;
    private Long totalOriginalPrice;
    private Long totalProductDiscountAmount;
    private Long totalPaymentAmount;
    private LocalDateTime expiresAt;

    private OrderSheet(String sheetId, List<OrderSheetItem> items, LocalDateTime createdAt) {
        this.sheetId = sheetId;
        this.items = items;
        this.expiresAt = createdAt.plusMinutes(30);
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

    private Long calcTotalOriginalPrice(List<OrderSheetItem> items) {
        return items.stream().mapToLong(OrderSheetItem::getOriginalLineTotal).sum();
    }

    private Long calcTotalProductDiscountAmount(List<OrderSheetItem> items) {
        return items.stream().mapToLong(OrderSheetItem::getDiscountLineTotal).sum();
    }

    private Long calcTotalPaymentAmount(List<OrderSheetItem> items) {
        return items.stream().mapToLong(OrderSheetItem::getLineTotal).sum();
    }
}
