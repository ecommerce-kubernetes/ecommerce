package com.example.product_service.product.domain.vo;

import com.example.product_service.common.domain.vo.Money;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepresentativePrice {
    private Money price;

    private Money originalPrice;

    private Long discountRate;
}
