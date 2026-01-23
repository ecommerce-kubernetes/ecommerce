package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderPriceCalculator {

    public ItemCalculationResult calculateItemAmounts(List<CreateOrderItemCommand> items, List<OrderProductResponse> products) {
        Map<Long, Integer> quantityByVariantId = mapToQuantityByVariantId(items);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = mapToUnitPriceByVariantId(products);
        return ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);
    }

    public PriceCalculateResult calculateFinalPrice(long useToPoint, ItemCalculationResult itemCalculationResult, long expectedPrice,
                                                    OrderUserResponse user, OrderCouponDiscountResponse coupon) {
        verifyEnoughPoints(useToPoint, user);
        long couponDiscount = coupon != null ? coupon.getDiscountAmount() : 0L;
        long priceAfterCoupon = itemCalculationResult.getSubTotalPrice() - couponDiscount;
        long finalPaymentAmount = priceAfterCoupon - useToPoint;

        if(finalPaymentAmount != expectedPrice) {
            throw new BusinessException(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }

        return PriceCalculateResult.of(itemCalculationResult, coupon, couponDiscount, useToPoint, finalPaymentAmount);
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
            throw new BusinessException(OrderErrorCode.ORDER_USER_INSUFFICIENT_POINT_BALANCE);
        }
    }

    private Map<Long, Integer> mapToQuantityByVariantId(List<CreateOrderItemCommand> items){
        return  items.stream()
                .collect(Collectors.toMap(
                        CreateOrderItemCommand::getProductVariantId,
                        CreateOrderItemCommand::getQuantity
                ));
    }

}
