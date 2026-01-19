package com.example.product_service.api.product.domain.repository.query;

import com.querydsl.core.types.OrderSpecifier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

import static com.example.product_service.api.product.domain.model.QProduct.product;

public class ProductQueryMapper {

    @AllArgsConstructor
    private enum SortType {
        LATEST("latest", product.publishedAt.desc()),
        OLDEST("oldest", product.publishedAt.asc()),
        HIGH_RATING("high-rating", product.rating.desc()),
        LOW_RATING("low-rating", product.rating.asc()),
        HIGH_PRICE("high-price", product.lowestPrice.desc()),
        LOW_PRICE("low-price", product.lowestPrice.asc()),
        SPECIAL_SALE("special-sale", product.maxDiscountRate.desc()),
        POPULARITY("popularity", product.popularityScore.desc());

        private final String code;
        @Getter
        private final OrderSpecifier<?> specifier;

        static SortType fromCode(String code) {
            return Arrays.stream(values())
                    .filter(type -> type.code.equalsIgnoreCase(code))
                    .findFirst()
                    .orElse(LATEST);
        }
    }

    public static OrderSpecifier<?> toOrderSpecifier(String code) {
        return SortType.fromCode(code).getSpecifier();
    }
}
