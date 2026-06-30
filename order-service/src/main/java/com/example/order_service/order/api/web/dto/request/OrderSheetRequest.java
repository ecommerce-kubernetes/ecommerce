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
            @NotEmpty(message = "주문 상품은 한개 이상이여야 합니다")
            List<OrderItem> items,
            Long cartCouponId,
            @Valid
            @NotNull(message = "상품 쿠폰은 필수값 입니다")
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
            @NotNull(message = "productVariantId는 필수값입니다")
            Long productVariantId,
            @NotNull(message = "quantity는 필수값입니다")
            @Min(value = 1, message = "quantity는 1이상 이여야 합니다")
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
            @NotNull(message = "쿠폰을 적용할 상품 변형 아이디는 필수값 입니다")
            Long productVariantId,
            @NotNull(message = "적용할 쿠폰 아이디는 필수값 입니다")
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
            @NotBlank(message = "수령인 이름은 필수입니다")
            String receiverName,
            @Pattern(regexp = "^01[016-9]-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
            @NotBlank(message = "수령인 전화번호는 필수입니다")
            String receiverPhone,
            @NotBlank(message = "우편 번호는 필수입니다")
            @Pattern(regexp = "^[0-9]{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
            String zipCode,
            @NotBlank(message = "기본 주소는 필수입니다")
            String address,
            @NotBlank(message = "상세 주소는 필수입니다")
            String addressDetail
    ) {
        public OrderSheetCommand.UpdateShippingAddress toCommand(String sheetId, Long userId) {
            return OrderSheetCommand.UpdateShippingAddress.of(sheetId, userId, receiverName, receiverPhone, zipCode, address, addressDetail);
        }
    }

    @Builder
    public record UpdateUsedPoints(
            @NotNull(message = "사용 포인트는 필수입니다")
            @Min(value = 0, message = "사용 포인트는 0 미만일 수 없습니다")
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
