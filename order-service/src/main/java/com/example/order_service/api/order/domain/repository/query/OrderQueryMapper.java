package com.example.order_service.api.order.domain.repository.query;

import com.querydsl.core.types.OrderSpecifier;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

import static com.example.order_service.api.order.domain.model.QOrder.order;

public class OrderQueryMapper {
    @AllArgsConstructor
    private enum SortType {
        LATEST("latest", order.createdAt.desc()),
        OLDEST("oldest", order.createdAt.asc());

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
