package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.application.dto.context.PriceCalculateResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderPriceCalculator {

    public long calculateRowTotalPrice(List<CreateOrderItemDto> items, List<OrderProductResponse> products) {
        Map<Long, Integer> quantityByVariantId = items.stream()
                .collect(Collectors.toMap(
                        CreateOrderItemDto::getProductVariantId,
                        CreateOrderItemDto::getQuantity
                ));

        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = products.stream()
                .collect(Collectors.toMap(
                        OrderProductResponse::getProductVariantId,
                        OrderProductResponse::getUnitPrice
                ));

        return quantityByVariantId.entrySet().stream()
                .mapToLong(
                        entry -> entry.getValue() * unitPriceByVariantId
                                .get(entry.getKey()).getDiscountedPrice())
                .sum();
    }

    public PriceCalculateResult calculateFinalPrice(OrderCouponCalcResponse coupon){
        return null;
    }
}
