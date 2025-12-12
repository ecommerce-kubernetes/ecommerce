package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.domain.service.dto.result.PriceCalculateResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderPriceCalculator {

    public long calculateSubTotalPrice(List<CreateOrderItemDto> items, List<OrderProductResponse> products) {
        Map<Long, Integer> quantityByVariantId = mapToQuantityByVariantId(items);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = mapToUnitPriceByVariantId(products);
        return quantityByVariantId.entrySet().stream()
                .mapToLong(
                        entry -> entry.getValue() * unitPriceByVariantId
                                .get(entry.getKey()).getDiscountedPrice())
                .sum();
    }

    public PriceCalculateResult calculateFinalPrice(long useToPoint, long subTotal, long expectedPrice,
                                                    OrderUserResponse user, OrderCouponCalcResponse coupon) {
        verifyEnoughPoints(useToPoint, user);
        long discountAmount = coupon != null ? coupon.getDiscountAmount() : 0L;
        long priceAfterCoupon = subTotal - discountAmount;
        long finalPaymentPrice = priceAfterCoupon - useToPoint;

        if(finalPaymentPrice != expectedPrice) {
            throw new OrderVerificationException("주문 금액이 변동되었습니다");
        }
        return PriceCalculateResult.of(subTotal, finalPaymentPrice, coupon, useToPoint);
    }

    private Map<Long, OrderProductResponse.UnitPrice> mapToUnitPriceByVariantId(List<OrderProductResponse> products) {
        return products.stream()
                .collect(Collectors.toMap(
                        OrderProductResponse::getProductVariantId,
                        OrderProductResponse::getUnitPrice
                ));
    }

    private void verifyEnoughPoints(Long useToPoint, OrderUserResponse user){
        if(useToPoint > 0 && !user.hasEnoughPoints(useToPoint)) {
            throw new InsufficientException("포인트가 부족합니다");
        }
    }

    private Map<Long, Integer> mapToQuantityByVariantId(List<CreateOrderItemDto> items){
        return  items.stream()
                .collect(Collectors.toMap(
                        CreateOrderItemDto::getProductVariantId,
                        CreateOrderItemDto::getQuantity
                ));
    }

}
