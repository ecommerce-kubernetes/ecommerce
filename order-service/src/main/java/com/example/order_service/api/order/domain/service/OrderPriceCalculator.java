package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.service.dto.result.OrderPriceInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderPriceCalculator {

    public OrderProductAmount calculateItemAmounts(List<CreateOrderItemCommand> items, List<OrderProductInfo> products) {
        Map<Long, Integer> qtyByVariantId = mapToQuantityByVariantId(items);
        long totalOriginalAmount = 0;
        long totalDiscountAmount = 0;
        long subTotalAmount= 0;
        for(OrderProductInfo p : products) {
            Integer qty = qtyByVariantId.get(p.getProductVariantId());
            totalOriginalAmount += p.getOriginalPrice() * qty;
            totalDiscountAmount += p.getDiscountAmount() * qty;
            subTotalAmount += p.getDiscountedPrice() * qty;
        }
        return OrderProductAmount.of(totalOriginalAmount, totalDiscountAmount, subTotalAmount);
    }

    public OrderPriceInfo calculateOrderPrice(OrderProductAmount orderProductAmount, OrderCouponInfo coupon,
                                              long pointToUse, long expectedPrice) {
        long finalPaymentAmount = orderProductAmount.getSubTotalAmount() - pointToUse - coupon.getDiscountAmount();

        if (finalPaymentAmount != expectedPrice) {
            throw new BusinessException(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }

        return OrderPriceInfo.of(
                orderProductAmount.getTotalOriginalAmount(),
                orderProductAmount.getTotalDiscountAmount(),
                coupon.getDiscountAmount(),
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
