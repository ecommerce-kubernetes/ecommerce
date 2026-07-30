package com.example.order_service.order.domain.vo;

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
public class ProductPriceSnapshot {

    private Money originalPrice;

    private Integer discountRate;

    private Money discountAmount;

    private Money discountedPrice;

    private ProductPriceSnapshot(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice = discountedPrice;
    }

    public static ProductPriceSnapshot of(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        Assert.notNull(originalPrice, "상품 원 가격은 필수 입니다.");
        Assert.notNull(discountRate, "상품 할인율은 필수 입니다.");
        Assert.notNull(discountAmount, "상품 할인 금액은 필수 입니다.");
        Assert.notNull(discountedPrice, "상품 판매 가격은 필수 입니다.");

        if (discountRate < 0 || discountRate > 100) {
            throw new BusinessException(OrderErrorCode.INVALID_PRODUCT_DISCOUNT_RATE);
        }
        if (originalPrice.isLessThan(discountAmount)) {
            throw new BusinessException(OrderErrorCode.INVALID_PRODUCT_DISCOUNT_AMOUNT);
        }
        if (!originalPrice.subtract(discountAmount).equals(discountedPrice)) {
            throw new BusinessException(OrderErrorCode.INVALID_PRODUCT_DISCOUNTED_PRICE);
        }
        return new ProductPriceSnapshot(originalPrice, discountRate, discountAmount, discountedPrice);
    }
}
