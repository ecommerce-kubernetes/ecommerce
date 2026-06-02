package com.example.order_service.order.infrastructure.persistence;

import com.querydsl.core.types.OrderSpecifier;
import lombok.AllArgsConstructor;

import java.util.Arrays;

import static com.example.order_service.order.domain.model.QOrder.order;

public class OrderQueryMapper {
    @AllArgsConstructor
    private enum SortType {
        LATEST("latest") {
            @Override
            public OrderSpecifier<?> getSpecifier() {
                return order.createdAt.desc();
            }
        },
        OLDEST("oldest") {
            @Override
            public OrderSpecifier<?> getSpecifier(){
                return order.createdAt.asc();
            }
        };

        private final String code;
        public abstract OrderSpecifier<?> getSpecifier();

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
