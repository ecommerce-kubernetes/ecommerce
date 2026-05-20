package com.example.order_service.ordersheet.application.dto.command;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import lombok.Builder;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderSheetCommand {

    @Builder
    public record Create(
            Long userId,
            List<OrderItem> items,
            Long cartCouponId,
            List<ItemCoupon> itemCoupons
    ) {
        public Create {
            if (items == null || items.isEmpty()) {
                throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_ITEMS_REQUIRED);
            }
            Set<Long> orderItemVariantIds = new HashSet<>();
            for (OrderItem item : items) {
                if (!orderItemVariantIds.add(item.productVariantId())) {
                    throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_ITEMS_DUPLICATE);
                }
            }
            if (itemCoupons != null && !itemCoupons.isEmpty()) {
                Set<Long> couponItemIds = new HashSet<>();
                Set<Long> itemCouponIds = new HashSet<>();
                for (ItemCoupon coupon : itemCoupons) {
                    if (!orderItemVariantIds.contains(coupon.productVariantId())) {
                        throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_COUPON_ITEM_NOT_IN_ITEMS);
                    }
                    if (!couponItemIds.add(coupon.productVariantId())) {
                        throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_DUPLICATE_COUPON_APPLICATION);
                    }
                    if (!itemCouponIds.add(coupon.couponId())) {
                        throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_ALREADY_APPLIED_TO_ANOTHER_ITEM);
                    }
                }
            }
        }

        public Map<Long, Long> toCouponMap() {
            return itemCoupons.stream()
                    .collect(Collectors.toMap(ItemCoupon::productVariantId, ItemCoupon::couponId));
        }

        public boolean hasCoupons() {
            return this.cartCouponId != null || (this.itemCoupons != null && !this.itemCoupons.isEmpty());
        }
    }

    @Builder
    public record OrderItem(
            Long productVariantId,
            Integer quantity
    ) {
    }

    @Builder
    public record ItemCoupon(
            Long productVariantId,
            Long couponId
    ) {
    }

    @Builder
    public record CouponCalculate(
            Long userId,
            Long cartCouponId,
            List<AppliedCouponItem> items
    ) {
        public static CouponCalculate of(Long userId, Long cartCouponId, List<AppliedCouponItem> items) {
            return CouponCalculate.builder()
                    .userId(userId)
                    .cartCouponId(cartCouponId)
                    .items(items)
                    .build();
        }
    }

    @Builder
    public record AppliedCouponItem(
            Long productVariantId,
            Money discountedPrice,
            Integer quantity,
            Long itemCouponId
    ) {
        public static AppliedCouponItem of(Long productVariantId, Money discountedPrice, Integer quantity, Long itemCouponId) {
            return AppliedCouponItem.builder()
                    .productVariantId(productVariantId)
                    .discountedPrice(discountedPrice)
                    .quantity(quantity)
                    .itemCouponId(itemCouponId)
                    .build();
        }
    }

    @Builder
    public record UpdateShippingAddress(
            String sheetId,
            Long userId,
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
        public static UpdateShippingAddress of(String sheetId, Long userId, String receiverName, String receiverPhone, String zipCode, String address, String addressDetail) {
            return UpdateShippingAddress.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .receiverName(receiverName)
                    .receiverPhone(receiverPhone)
                    .zipCode(zipCode)
                    .address(address)
                    .addressDetail(addressDetail)
                    .build();
        }
    }

    @Builder
    public record UpdatePoints(
            String sheetId,
            Long userId,
            Money usedPoints
    ) {
        public static UpdatePoints of(String sheetId, Long userId, Long usedPoints) {
            return UpdatePoints.builder()
                    .sheetId(sheetId)
                    .userId(userId)
                    .usedPoints(Money.wons(usedPoints))
                    .build();
        }
    }

    @Builder
    public record UpdateItemCoupon(
            String sheetId,
            String sheetItemId,
            Long userId,
            Long couponId
    ) {
        public static UpdateItemCoupon of(String sheetId, String sheetItemId, Long userId, Long couponId) {
            return UpdateItemCoupon.builder()
                    .sheetId(sheetId)
                    .sheetItemId(sheetItemId)
                    .userId(userId)
                    .couponId(couponId)
                    .build();
        }
    }
}
