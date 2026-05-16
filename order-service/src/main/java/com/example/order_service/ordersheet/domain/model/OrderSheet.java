package com.example.order_service.ordersheet.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import com.example.order_service.ordersheet.domain.model.vo.OrderCouponSnapshot;
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
    private Long userId;
    private List<OrderSheetItem> items;
    private OrderCouponSnapshot cartCoupon;
    private Money totalOriginalPrice;
    private Money totalProductDiscountAmount;
    private Money totalCouponDiscountAmount;
    private Money usedPoints;
    private Money totalPaymentAmount;
    private LocalDateTime expiresAt;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheet(String sheetId, Long userId, List<OrderSheetItem> items, OrderCouponSnapshot cartCoupon,
                       Money totalOriginalPrice, Money totalProductDiscountAmount, Money totalCouponDiscountAmount,
                       Money usedPoints, Money totalPaymentAmount, LocalDateTime expiresAt) {
        this.sheetId = sheetId;
        this.userId = userId;
        this.items = items;
        this.cartCoupon = cartCoupon;
        this.totalOriginalPrice = totalOriginalPrice;
        this.totalProductDiscountAmount = totalProductDiscountAmount;
        this.totalCouponDiscountAmount = totalCouponDiscountAmount;
        this.usedPoints = usedPoints;
        this.totalPaymentAmount = totalPaymentAmount;
        this.expiresAt = expiresAt;
    }

    public static OrderSheet create(String sheetId, Long userId, List<OrderSheetItem> items, OrderCouponSnapshot coupon, LocalDateTime createdAt, long ttl) {
        if (items == null || items.isEmpty()) {
            throw new InvalidDomainValueException("OrderSheet 주문 상품은 필수입니다");
        }
        return OrderSheet.reconstitute()
                .sheetId(sheetId)
                .userId(userId)
                .items(items)
                .cartCoupon(coupon)
                .totalOriginalPrice(calcTotalOriginalPrice(items))
                .totalProductDiscountAmount(calcTotalProductDiscountAmount(items))
                .totalCouponDiscountAmount(coupon.getDiscountAmount().add(calcTotalItemCouponDiscountAmount(items)))
                .usedPoints(Money.ZERO)
                .totalPaymentAmount(calcTotalPaymentAmount(items, coupon, Money.ZERO))
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

    private static Money calcTotalPaymentAmount(List<OrderSheetItem> items, OrderCouponSnapshot coupon, Money usedPoints) {
        Money itemFinalPrice = items.stream()
                .map(OrderSheetItem::getFinalLineTotal)
                .reduce(Money.ZERO, Money::add);
        Money subTotal = itemFinalPrice.subtract(coupon.getDiscountAmount());
        return subTotal.subtract(usedPoints);
    }

    private static Money calcTotalItemCouponDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getCouponDiscount)
                .reduce(Money.ZERO, Money::add);
    }
}
