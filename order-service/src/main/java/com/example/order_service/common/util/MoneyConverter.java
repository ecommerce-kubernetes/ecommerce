package com.example.order_service.common.util;

import com.example.order_service.common.domain.vo.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Long> {
    @Override
    public Long convertToDatabaseColumn(Money money) {
        if (money == null) {
            return null;
        }
        return money.longValue();
    }

    @Override
    public Money convertToEntityAttribute(Long aLong) {
        if (aLong == null) {
            return Money.ZERO;
        }
        return Money.wons(aLong);
    }
}
