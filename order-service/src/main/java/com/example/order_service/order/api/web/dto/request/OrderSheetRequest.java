package com.example.order_service.order.api.web.dto.request;

import com.example.order_service.order.application.service.ordersheet.dto.command.OrderSheetCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.List;

public class OrderSheetRequest {

    @Builder
    public record Create(
            @Valid
            @NotEmpty(message = "{orderSheet.items.notEmpty}")
            List<OrderItem> items,
            Long cartCouponId,
            @Valid
            @NotNull(message = "{orderSheet.itemCoupons.notNull}")
            List<ItemCoupon> itemCoupons
    ) {

        public OrderSheetCommand.Create toCommand(Long userId) {
            return OrderSheetCommand.Create.builder()
                    .userId(userId)
                    .items(itemsMapToCommand(items))
                    .cartCouponId(cartCouponId)
                    .itemCoupons(couponsMapToCommand(itemCoupons))
                    .build();
        }

        private List<OrderSheetCommand.OrderItem> itemsMapToCommand(List<OrderItem> items) {
            return items.stream().map(OrderItem::toCommand).toList();
        }

        private List<OrderSheetCommand.ItemCoupon> couponsMapToCommand(List<ItemCoupon> coupons) {
            return coupons.stream().map(ItemCoupon::toCommand).toList();
        }
    }

    @Builder(toBuilder = true)
    public record OrderItem(
            @NotNull(message = "{orderSheet.item.productVariantId.notNull}")
            Long productVariantId,
            @NotNull(message = "{orderSheet.item.quantity.notNull}")
            @Min(value = 1, message = "{orderSheet.item.quantity.min}")
            Integer quantity
    ) {
        public OrderSheetCommand.OrderItem toCommand() {
            return OrderSheetCommand.OrderItem.builder()
                    .productVariantId(productVariantId)
                    .quantity(quantity)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record ItemCoupon(
            @NotNull(message = "{orderSheet.itemCoupon.productVariantId.notNull}")
            Long productVariantId,
            @NotNull(message = "{orderSheet.itemCoupon.couponId.notNull}")
            Long couponId
    ) {
        public OrderSheetCommand.ItemCoupon toCommand() {
            return OrderSheetCommand.ItemCoupon.builder()
                    .productVariantId(productVariantId)
                    .couponId(couponId)
                    .build();
        }
    }

    @Builder(toBuilder = true)
    public record UpdateShippingAddress(
            @NotBlank(message = "{orderSheet.receiverName.notNull}")
            String receiverName,
            @Pattern(regexp = "^01[016-9]-\\d{3,4}-\\d{4}$", message = "{orderSheet.receiverPhone.pattern}")
            @NotBlank(message = "{orderSheet.receiverPhone.notNull}")
            String receiverPhone,
            @NotBlank(message = "{orderSheet.zipCode.notNull}")
            @Pattern(regexp = "^[0-9]{5}$", message = "{orderSheet.zipCode.pattern}")
            String zipCode,
            @NotBlank(message = "{orderSheet.address.notNull}")
            String address,
            @NotBlank(message = "{orderSheet.addressDetail.notNull}")
            String addressDetail
    ) {
        public OrderSheetCommand.UpdateShippingAddress toCommand(String sheetId, Long userId) {
            return OrderSheetCommand.UpdateShippingAddress.of(sheetId, userId, receiverName, receiverPhone, zipCode, address, addressDetail);
        }
    }

    @Builder
    public record UpdateUsedPoints(
            @NotNull(message = "{orderSheet.usedPoints.notNull}")
            @Min(value = 0, message = "{orderSheet.usedPoints.min}")
            Long usedPoints
    ) {
        public OrderSheetCommand.UpdatePoints toCommand(String sheetId, Long userId) {
            return OrderSheetCommand.UpdatePoints.of(sheetId, userId, usedPoints);
        }
    }

    @Builder
    public record UpdateCoupon(
            Long couponId
    ) {
        public OrderSheetCommand.UpdateItemCoupon toCommand(String sheetId, String sheetItemId, Long userId) {
            return OrderSheetCommand.UpdateItemCoupon.of(sheetId, sheetItemId, userId, couponId);
        }

        public OrderSheetCommand.UpdateCartCoupon toCommand(String sheetId, Long userId) {
            return OrderSheetCommand.UpdateCartCoupon.of(sheetId, userId, couponId);
        }
    }
}
