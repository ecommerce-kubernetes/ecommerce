package com.example.order_service.order.application.service.validator;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.application.port.dto.*;
import com.example.order_service.order.exception.OrderErrorCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderValidator {

    public void validateMissingCartItems(List<Long> requestIds, OrderCartItemsResult result) {
        if (requestIds.size() != result.items().size()) {
            throw new BusinessException(OrderErrorCode.CART_ITEM_NOT_FOUND);
        }
    }

    public void validateOrderable(OrderProductsResult.OrderProductDetail product, int requestQuantity) {
        if (product == null) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_NOT_FOUND);
        }

        if (product.status() != OrderProductStatus.ON_SALE) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_UNORDERABLE);
        }

        if (requestQuantity > product.stock()) {
            throw new BusinessException(OrderErrorCode.ORDER_PRODUCT_INSUFFICIENT_STOCK);
        }
    }

    public void validateAvailablePoints(Money availablePoints, Money usedPoints) {
        if (availablePoints.isLessThan(usedPoints)) {
            throw new BusinessException(OrderErrorCode.EXCEED_AVAILABLE_POINTS);
        }
    }

    public void validateItemCoupon(ItemCouponsResult.ItemCouponResult couponResult, LocalDateTime currentTime) {
        if (couponResult == null) {
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_NOT_FOUND);
        }

        if(couponResult.status() != OrderCouponStatus.AVAILABLE) {
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_UNAVAILABLE);
        }

        if (couponResult.expiresAt().isBefore(currentTime)){
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_EXPIRED);
        }
    }
}
