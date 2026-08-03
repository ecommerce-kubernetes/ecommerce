package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.springframework.util.Assert;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderAmount {

    private Money totalOriginalAmount;

    private Money totalItemDiscount;

    private Money totalItemCouponDiscount;

    private Money cartCouponDiscount;

    private Money usedPoints;

    private Money totalPaymentAmount;

    private OrderAmount(Money totalOriginalAmount, Money totalItemDiscount, Money totalItemCouponDiscount,
                        Money cartCouponDiscount, Money usedPoints, Money totalPaymentAmount) {
        this.totalOriginalAmount = totalOriginalAmount;
        this.totalItemDiscount = totalItemDiscount;
        this.totalItemCouponDiscount = totalItemCouponDiscount;
        this.cartCouponDiscount = cartCouponDiscount;
        this.usedPoints = usedPoints;
        this.totalPaymentAmount = totalPaymentAmount;
    }

    public static OrderAmount of(Money totalOriginalAmount, Money totalItemDiscount, Money totalItemCouponDiscount,
                                 Money cartCouponDiscount, Money usedPoints, Money totalPaymentAmount) {
        Assert.notNull(totalOriginalAmount, "총 주문 상품 원 가격은 필수 입니다.");
        Assert.notNull(totalItemDiscount, "총 상품 할인 가격은 필수 입니다.");
        Assert.notNull(totalItemCouponDiscount, "총 상품 쿠폰 할인 가격은 필수 입니다.");
        Assert.notNull(cartCouponDiscount, "장바구니 쿠폰 할인 가격은 필수 입니다.");
        Assert.notNull(usedPoints, "적용 포인트는 필수 입니다.");
        Assert.notNull(totalPaymentAmount, "최종 결제 금액은 필수 입니다.");

        Money totalReductions = totalItemDiscount
                .add(totalItemCouponDiscount)
                .add(cartCouponDiscount)
                .add(usedPoints);

        if (totalOriginalAmount.isLessThan(totalReductions)) {
            throw new BusinessException(OrderErrorCode.ORDER_DISCOUNT_EXCEEDS_TOTAL_AMOUNT);
        }

        if (!totalOriginalAmount.subtract(totalReductions).equals(totalPaymentAmount)) {
            throw new BusinessException(OrderErrorCode.INVALID_TOTAL_PAYMENT_AMOUNT);
        }

        return new OrderAmount(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, usedPoints, totalPaymentAmount);
    }
}
