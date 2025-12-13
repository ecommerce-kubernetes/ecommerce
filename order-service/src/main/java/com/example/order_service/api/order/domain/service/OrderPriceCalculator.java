package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.InsufficientException;
import com.example.order_service.api.common.exception.OrderVerificationException;
import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderPriceCalculator {

    public ItemCalculationResult calculateItemAmounts(List<CreateOrderItemDto> items, List<OrderProductResponse> products) {
        Map<Long, Integer> quantityByVariantId = mapToQuantityByVariantId(items);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = mapToUnitPriceByVariantId(products);
        long totalOriginPrice = 0;
        long totalProductDiscount = 0;
        long subTotalPrice = 0;
        for (Map.Entry<Long, Integer> entry : quantityByVariantId.entrySet()) {
            Long productVariantId = entry.getKey();
            OrderProductResponse.UnitPrice unitPrice = unitPriceByVariantId.get(productVariantId);

            totalOriginPrice += unitPrice.getOriginalPrice() * entry.getValue();
            totalProductDiscount += unitPrice.getDiscountAmount() * entry.getValue();
            subTotalPrice += unitPrice.getDiscountedPrice() * entry.getValue();
        }
        return ItemCalculationResult.of(totalOriginPrice, totalProductDiscount, subTotalPrice);
    }

    public PriceCalculateResult calculateFinalPrice(long useToPoint, ItemCalculationResult itemCalculationResult, long expectedPrice,
                                                    OrderUserResponse user, OrderCouponCalcResponse coupon) {
        verifyEnoughPoints(useToPoint, user);
        long couponDiscount = coupon != null ? coupon.getDiscountAmount() : 0L;
        long priceAfterCoupon = itemCalculationResult.getSubTotalPrice() - couponDiscount;
        long finalPaymentPrice = priceAfterCoupon - useToPoint;

        if(finalPaymentPrice != expectedPrice) {
            throw new OrderVerificationException("주문 금액이 변동되었습니다");
        }
        return PriceCalculateResult.from(itemCalculationResult, coupon, useToPoint, finalPaymentPrice);
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
