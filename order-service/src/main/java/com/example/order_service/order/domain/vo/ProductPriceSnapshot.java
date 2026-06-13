package com.example.order_service.order.domain.vo;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPriceSnapshot {
    private Money originalPrice;
    private Integer discountRate;
    private Money discountAmount;
    private Money discountedPrice;

    @Builder(builderMethodName = "reconstitute")
    private ProductPriceSnapshot(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountedPrice = discountedPrice;
    }

    public static ProductPriceSnapshot of(Money originalPrice, Integer discountRate, Money discountAmount, Money discountedPrice) {
        if (originalPrice == null || discountRate == null || discountAmount == null || discountedPrice == null) {
            throw new IllegalArgumentException("상품 가격 정보 누락");
        }
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("할인율은 0 ~ 100 의 값이여야 합니다");
        }
        if (originalPrice.isLessThan(discountAmount)) {
            throw new IllegalArgumentException("상품 할인 금액이 상품 금액을 초과할 수 없습니다");
        }
        if (!originalPrice.subtract(discountAmount).equals(discountedPrice)) {
            throw new IllegalArgumentException("상품 판매 가격이 올바르지 않습니다");
        }
        return new ProductPriceSnapshot(originalPrice, discountRate, discountAmount, discountedPrice);
    }
}
