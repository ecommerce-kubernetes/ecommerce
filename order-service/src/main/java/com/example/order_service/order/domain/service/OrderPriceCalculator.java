package com.example.order_service.order.domain.service;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.dto.command.CreateOrderItemCommand;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.domain.service.dto.result.CalculatedOrderAmounts;
import com.example.order_service.order.domain.service.dto.result.OrderProductAmount;
import com.example.order_service.order.exception.OrderErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderPriceCalculator {

    public OrderProductAmount calculateItemAmounts(List<CreateOrderItemCommand> items, List<OrderProductResult.Info> products) {
        Map<Long, Integer> qtyByVariantId = mapToQuantityByVariantId(items);
        long totalOriginalAmount = 0;
        long totalDiscountAmount = 0;
        long subTotalAmount= 0;
        for(OrderProductResult.Info p : products) {
            Integer qty = qtyByVariantId.get(p.productVariantId());
            totalOriginalAmount += p.originalPrice() * qty;
            totalDiscountAmount += p.discountAmount() * qty;
            subTotalAmount += p.discountedPrice() * qty;
        }
        return OrderProductAmount.of(totalOriginalAmount, totalDiscountAmount, subTotalAmount);
    }

    public CalculatedOrderAmounts calculateOrderPrice(OrderProductAmount orderProductAmount, OrderCouponResult.CouponValidation coupon,
                                                      long pointToUse, long expectedPrice) {
        long finalPaymentAmount = orderProductAmount.getSubTotalAmount() - pointToUse - coupon.couponBenefit().discountAmount();

        if (finalPaymentAmount != expectedPrice) {
            throw new BusinessException(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }

        return CalculatedOrderAmounts.of(
                orderProductAmount.getTotalOriginalAmount(),
                orderProductAmount.getTotalDiscountAmount(),
                coupon.couponBenefit().discountAmount(),
                pointToUse,
                finalPaymentAmount
        );
    }

    private Map<Long, Integer> mapToQuantityByVariantId(List<CreateOrderItemCommand> items){
        return  items.stream()
                .collect(Collectors.toMap(
                        CreateOrderItemCommand::getProductVariantId,
                        CreateOrderItemCommand::getQuantity
                ));
    }

}
