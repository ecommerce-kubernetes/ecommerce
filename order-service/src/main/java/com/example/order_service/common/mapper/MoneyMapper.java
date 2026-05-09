package com.example.order_service.common.mapper;

import com.example.order_service.common.domain.vo.Money;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MoneyMapper {
    default Money toMoney(Long amount) {
        if (amount == null) {
            return Money.ZERO;
        }
        return Money.wons(amount);
    }

    default Long toLong(Money money) {
        if (money == null) {
            return null;
        }
        return money.longValue();
    }
}
