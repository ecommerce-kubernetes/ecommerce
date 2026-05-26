package com.example.order_service.order.application.policy;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DefaultPointUsagePolicy implements PointUsagePolicy {
    private final OrderSheetProperties properties;
    @Override
    public Money calculateMaxLimit(Money pointEligibleAmount) {
        BigDecimal limitRate = properties.pointLimitRate();
        return pointEligibleAmount.multiple(limitRate);
    }
}
