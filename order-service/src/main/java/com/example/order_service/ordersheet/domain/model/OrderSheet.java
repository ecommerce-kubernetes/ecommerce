package com.example.order_service.ordersheet.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheet {
    private String sheetId;
    private List<OrderSheetItem> items;
    private Money totalOriginalPrice;
    private Money totalProductDiscountAmount;
    private Money totalPaymentAmount;
    private LocalDateTime expiresAt;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheet(String sheetId, List<OrderSheetItem> items,
                       Money totalOriginalPrice, Money totalProductDiscountAmount, Money totalPaymentAmount, LocalDateTime expiresAt) {
        this.sheetId = sheetId;
        this.items = items;
        this.totalOriginalPrice = totalOriginalPrice;
        this.totalProductDiscountAmount = totalProductDiscountAmount;
        this.totalPaymentAmount = totalPaymentAmount;
        this.expiresAt = expiresAt;
    }

    public static OrderSheet create(String sheetId, List<OrderSheetItem> items, LocalDateTime createdAt, long ttl) {
        if (items == null || items.isEmpty()) {
            throw new InvalidDomainValueException("OrderSheet 주문 상품은 필수입니다");
        }
        return OrderSheet.reconstitute()
                .sheetId(sheetId)
                .items(items)
                .totalOriginalPrice(calcTotalOriginalPrice(items))
                .totalProductDiscountAmount(calcTotalProductDiscountAmount(items))
                .totalPaymentAmount(calcTotalPaymentAmount(items))
                .expiresAt(createdAt.plusMinutes(ttl))
                .build();
    }

    private static Money calcTotalOriginalPrice(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getOriginalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcTotalProductDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getDiscountLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcTotalPaymentAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getLineTotal)
                .reduce(Money.ZERO, Money::add);
    }
}
