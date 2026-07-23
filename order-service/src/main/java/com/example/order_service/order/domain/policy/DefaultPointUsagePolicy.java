package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class DefaultPointUsagePolicy implements PointUsagePolicy {
    private final BigDecimal limitRate;

    @Override
    public Money calculateAvailablePoints(Money baseAmount) {
        return baseAmount.multiple(limitRate);
    }
}
