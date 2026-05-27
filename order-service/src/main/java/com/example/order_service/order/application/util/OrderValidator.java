package com.example.order_service.order.application.util;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.exception.OrderErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderValidator {
    public void validate(OrderSheet orderSheet,
                         OrderProductResult.ProductList products,
                         OrderCouponResult.Calculate coupon,
                         OrderUserResult.UserPoint userPoint,
                         PointUsagePolicy pointPolicy) {
        validateOrderProducts(orderSheet, products);
        validateCoupons(orderSheet, coupon);
        validateUserPoints(orderSheet, userPoint, pointPolicy);
    }

    private void validateOrderProducts(OrderSheet orderSheet, OrderProductResult.ProductList products) {
        List<OrderSheetItem> items = orderSheet.getItems();
        Map<Long, OrderProductResult.Info> productsMap = products.getProductsMap();
        for (OrderSheetItem item : items) {
            OrderProductResult.Info product = productsMap.get(item.getProductVariantId());
            if (!item.getDiscountedPrice().equals(product.discountedPrice())) {
                throw new BusinessException(OrderErrorCode.PRODUCT_PRICE_CHANGE);
            }
        }
    }

    private void validateCoupons(OrderSheet orderSheet, OrderCouponResult.Calculate coupon) {
        if (!orderSheet.getCartCoupon().getDiscountAmount().equals(coupon.cartCoupon().discountAmount())) {
            throw new BusinessException(OrderErrorCode.CART_COUPON_DISCOUNT_CHANGE);
        }
        List<OrderSheetItem> items = orderSheet.getItems();
        Map<Long, OrderCouponResult.ItemCoupon> itemCouponMap = coupon.toItemCouponMap();
        for (OrderSheetItem item : items) {
            OrderCouponResult.ItemCoupon itemCoupon = itemCouponMap.get(item.getProductVariantId());
            if (!item.getAppliedCouponDiscount().equals(itemCoupon.discountAmount())){
                throw new BusinessException(OrderErrorCode.ITEM_COUPON_DISCOUNT_CHANGE);
            }
        }
    }

    private void validateUserPoints(OrderSheet orderSheet, OrderUserResult.UserPoint points, PointUsagePolicy pointPolicy) {
        Money availablePoints = orderSheet.calcAvailablePoints(points.ownedPoints(), pointPolicy);
        if (orderSheet.getUsedPoints().isGreaterThan(availablePoints)) {
            throw new BusinessException(OrderErrorCode.POINTS_DISCOUNT_CHANGE);
        }
    }
}
