package com.example.order_service.order.application.service.fixture;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.order.application.port.dto.OrdererPointResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.domain.vo.Orderer;

public class OrderUserResultFixture {

    public static OrdererProfileResult.OrdererProfileResultBuilder anOrdererProfile() {
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        return OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(Money.wons(1000L));
    }

    public static OrdererPointResult.OrdererPointResultBuilder anOrdererPointResult() {
        return OrdererPointResult.builder()
                .userId(1L)
                .availablePoints(Money.wons(10000L));
    }
}
