package com.example.order_service.ordersheet.domain.model;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.domain.InvalidDomainValueException;
import com.example.order_service.ordersheet.domain.model.vo.OrderCouponSnapshot;
import com.example.order_service.ordersheet.domain.model.vo.Orderer;
import com.example.order_service.ordersheet.domain.model.vo.ShippingAddress;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderSheet {
    private String sheetId;
    private Orderer orderer;
    private ShippingAddress shippingAddress;
    private List<OrderSheetItem> items;
    private OrderCouponSnapshot cartCoupon;
    private Money totalOriginalPrice;
    private Money totalProductDiscountAmount;
    private Money totalCouponDiscountAmount;
    private Money usedPoints;
    private Money totalPaymentAmount;
    private LocalDateTime expiresAt;

    @Builder(builderMethodName = "reconstitute")
    private OrderSheet(String sheetId, Orderer orderer, ShippingAddress shippingAddress, List<OrderSheetItem> items, OrderCouponSnapshot cartCoupon,
                       Money totalOriginalPrice, Money totalProductDiscountAmount, Money totalCouponDiscountAmount,
                       Money usedPoints, Money totalPaymentAmount, LocalDateTime expiresAt) {
        this.sheetId = sheetId;
        this.orderer = orderer;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.cartCoupon = cartCoupon;
        this.totalOriginalPrice = totalOriginalPrice;
        this.totalProductDiscountAmount = totalProductDiscountAmount;
        this.totalCouponDiscountAmount = totalCouponDiscountAmount;
        this.usedPoints = usedPoints;
        this.totalPaymentAmount = totalPaymentAmount;
        this.expiresAt = expiresAt;
    }

    public static OrderSheet create(String sheetId, Orderer orderer, ShippingAddress shippingAddress, List<OrderSheetItem> items, OrderCouponSnapshot coupon, LocalDateTime createdAt, long ttl) {
        if (items == null || items.isEmpty()) {
            throw new InvalidDomainValueException("OrderSheet 주문 상품은 필수입니다");
        }

        Money pointEligibleAmount = calcPointEligibleAmount(items, coupon);

        return OrderSheet.reconstitute()
                .sheetId(sheetId)
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .items(items)
                .cartCoupon(coupon)
                .totalOriginalPrice(calcTotalOriginalPrice(items))
                .totalProductDiscountAmount(calcTotalProductDiscountAmount(items))
                .totalCouponDiscountAmount(calcAppliedCartCouponDiscount(items, coupon).add(calcTotalItemCouponDiscountAmount(items)))
                .usedPoints(Money.ZERO)
                .totalPaymentAmount(pointEligibleAmount)
                .expiresAt(createdAt.plusMinutes(ttl))
                .build();
    }

    public Money getPointEligibleAmount() {
        return calcPointEligibleAmount(this.items, this.cartCoupon);
    }

    private static Money calcTotalItemFinalPrice(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getFinalLineTotal)
                .reduce(Money.ZERO, Money::add);
    }

    private static Money calcPointEligibleAmount(List<OrderSheetItem> items, OrderCouponSnapshot coupon) {
        Money itemFinalPrice = calcTotalItemFinalPrice(items);
        return itemFinalPrice.isLessThan(coupon.getDiscountAmount()) ?
                Money.ZERO : itemFinalPrice.subtract(coupon.getDiscountAmount());
    }

    private static Money calcAppliedCartCouponDiscount(List<OrderSheetItem> items, OrderCouponSnapshot coupon) {
        Money itemFinalPrice = calcTotalItemFinalPrice(items);
        return itemFinalPrice.isLessThan(coupon.getDiscountAmount()) ? itemFinalPrice : coupon.getDiscountAmount();
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

    private static Money calcTotalItemCouponDiscountAmount(List<OrderSheetItem> items) {
        return items.stream()
                .map(OrderSheetItem::getAppliedCouponDiscount)
                .reduce(Money.ZERO, Money::add);
    }

    public boolean isOwner(Long userId) {
        return this.orderer.getUserId().equals(userId);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public Duration getRemainingTtl() {
        return Duration.between(LocalDateTime.now(), this.expiresAt);
    }

    public void changeShippingAddress(ShippingAddress newAddress) {
        this.shippingAddress = newAddress;
    }

    public void changeUsedPoints(Money usedPoints) {
        this.usedPoints = usedPoints;
        Money eligibleAmount = getPointEligibleAmount();
        this.totalPaymentAmount = eligibleAmount.isLessThan(usedPoints) ?
                Money.ZERO : eligibleAmount.subtract(usedPoints);
    }
}
