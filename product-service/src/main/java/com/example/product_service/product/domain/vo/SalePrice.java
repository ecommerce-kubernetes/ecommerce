package com.example.product_service.product.domain.vo;

import com.example.product_service.common.domain.vo.Money;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.springframework.util.Assert;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalePrice {
    private Money originalPrice;

    private Integer discountRate;

    private Money discountAmount;

    private Money price;

    @Builder(access = AccessLevel.PRIVATE)
    private SalePrice(Money originalPrice, Integer discountRate, Money discountAmount, Money price) {
        Assert.notNull(originalPrice, "판매 가격의 원본 가격은 필수이다");
        Assert.notNull(discountRate, "판매 가격의 할인율은 필수이다");
        Assert.notNull(discountAmount, "판매 가격의 할인 가격은 필수이다");
        Assert.notNull(price, "판매 가격의 판맥 가격은 필수이다");

        this.originalPrice = originalPrice;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.price = price;
    }

    public static SalePrice of(Money originalPrice, Integer discountRate, Money discountAmount, Money price) {
        return SalePrice.builder()
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .price(price)
                .build();
    }
}
