package com.example.order_service.order.domain.policy;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.infrastructure.config.OrderSheetProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPointUsagePolicyTest {

    @Test
    @DisplayName("사용 가능한 포인트를 계산한다")
    void calculateAvailablePoints() {
        //given
        BigDecimal limitRate = BigDecimal.valueOf(0.1);
        Money baseAmount = Money.wons(10000L);
        DefaultPointUsagePolicy policy = new DefaultPointUsagePolicy(limitRate);
        //when
        Money result = policy.calculateAvailablePoints(baseAmount);
        //then
        assertThat(result).isEqualTo(Money.wons(1000L));
    }
}