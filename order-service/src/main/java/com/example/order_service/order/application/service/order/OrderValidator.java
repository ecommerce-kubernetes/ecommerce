package com.example.order_service.order.application.service.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.policy.PointUsagePolicy;
import com.example.order_service.order.domain.vo.OrderCouponSnapshot;
import com.example.order_service.order.exception.OrderErrorCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 주문 유효성을 검증하는 주문 검증기
 * <p>
 * 주문의 정합성과 유효성을 검사하는 역할을 담당
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 28
 */
@Component
public class OrderValidator {

    /**
     * 주문서 검증
     * <p>
     * 상품 조회 결과, 쿠폰 검증 결과, 유저 포인트가 주문서의 금액 정보와 일치하는지 검증
     * </p>
     *
     * @param orderSheet  주문서
     * @param products    상품 조회 결과
     * @param coupon      쿠폰 검증 결과
     * @param userPoint   유저 포인트 조회 결과
     * @param pointPolicy 포인트 할인 정책
     */
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
            if (!item.getDiscountedPrice().equals(product.priceSnapshot().getDiscountedPrice())) {
                throw new BusinessException(OrderErrorCode.PRODUCT_PRICE_CHANGE);
            }
        }
    }

    private void validateCoupons(OrderSheet orderSheet, OrderCouponResult.Calculate coupon) {
        if (!orderSheet.getCartCoupon().getDiscountAmount().equals(coupon.cartCoupon().getDiscountAmount())) {
            throw new BusinessException(OrderErrorCode.CART_COUPON_DISCOUNT_CHANGE);
        }
        List<OrderSheetItem> items = orderSheet.getItems();
        Map<Long, OrderCouponSnapshot> itemCouponMap = coupon.toItemCouponMap();
        for (OrderSheetItem item : items) {
            OrderCouponSnapshot itemCoupon = itemCouponMap.get(item.getProductVariantId());
            if (!item.getAppliedCouponDiscount().equals(itemCoupon.getDiscountAmount())) {
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
