package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItemAmount {

    private Money originalAmount;

    private Money itemDiscount;

    private Money lineTotal;

    private Money itemCouponDiscount;

    private Money finalAmount;

    private OrderItemAmount(Money originalAmount, Money itemDiscount, Money lineTotal, Money itemCouponDiscount, Money finalAmount) {
        this.originalAmount = originalAmount;
        this.itemDiscount = itemDiscount;
        this.lineTotal = lineTotal;
        this.itemCouponDiscount = itemCouponDiscount;
        this.finalAmount = finalAmount;
    }

    public static OrderItemAmount of(Money originalAmount, Money itemDiscount, Money lineTotal, Money itemCouponDiscount, Money finalAmount) {
        Assert.notNull(originalAmount, "항목 원가 총액은 필수 입니다.");
        Assert.notNull(itemDiscount, "항목 상품 할인 총액은 필수 입니다.");
        Assert.notNull(lineTotal, "상품 판매가 총액은 필수 입니다.");
        Assert.notNull(itemCouponDiscount, "항목 상품 쿠폰 할인 금액은 필수 입니다.");
        Assert.notNull(finalAmount, "항목 최종 결제 금액은 필수 입니다.");

        if (originalAmount.isLessThan(itemDiscount)) {
            throw new BusinessException(OrderErrorCode.ITEM_DISCOUNT_EXCEEDS_ORIGINAL_AMOUNT);
        }

        if (!originalAmount.subtract(itemDiscount).equals(lineTotal)) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_ITEM_LINE_TOTAL);
        }

        if (lineTotal.isLessThan(itemCouponDiscount)) {
            throw new BusinessException(OrderErrorCode.ITEM_COUPON_DISCOUNT_EXCEEDS_LINE_TOTAL);
        }

        if (!lineTotal.subtract(itemCouponDiscount).equals(finalAmount)) {
            throw new BusinessException(OrderErrorCode.INVALID_ORDER_ITEM_FINAL_AMOUNT);
        }

        return new OrderItemAmount(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, finalAmount);
    }
}
