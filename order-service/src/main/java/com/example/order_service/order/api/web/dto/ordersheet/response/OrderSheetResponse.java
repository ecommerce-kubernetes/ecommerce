package com.example.order_service.order.api.web.dto.ordersheet.response;

import com.example.order_service.order.application.service.ordersheet.dto.result.OrderSheetResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSheetResponse(
        String orderSheetId,
        OrdererInfo orderer,
        ShippingAddressInfo shippingAddress,
        List<OrderSheetItemResponse> items,
        CartCouponInfo cartCoupon,
        PaymentSummaryInfo paymentSummary,
        PointInfo point,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime expiresAt
) {
    public static OrderSheetResponse from(OrderSheetResult result) {
        return OrderSheetResponse.builder()
                .orderSheetId(result.orderSheetId())
                .orderer(OrdererInfo.from(result.orderer()))
                .shippingAddress(ShippingAddressInfo.from(result.shippingAddress()))
                .items(OrderSheetItemResponse.from(result.items()))
                .cartCoupon(CartCouponInfo.from(result.cartCoupon()))
                .paymentSummary(PaymentSummaryInfo.from(result.paymentSummary()))
                .point(PointInfo.from(result.point()))
                .expiresAt(result.expiresAt())
                .build();
    }

    @Builder
    public record OrdererInfo(
            Long userId,
            String userName,
            String phoneNumber
    ) {

        public static OrdererInfo from(Orderer orderer) {
            return OrdererInfo.builder()
                    .userId(orderer.getUserId())
                    .userName(orderer.getUserName())
                    .phoneNumber(orderer.getPhoneNumber())
                    .build();
        }

    }

    @Builder
    public record ShippingAddressInfo(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
        public static ShippingAddressInfo from(ShippingAddress shippingAddress) {
            return ShippingAddressInfo.builder()
                    .receiverName(shippingAddress.getReceiverName())
                    .receiverPhone(shippingAddress.getReceiverPhone())
                    .zipCode(shippingAddress.getZipCode())
                    .address(shippingAddress.getAddress())
                    .addressDetail(shippingAddress.getAddressDetail())
                    .build();
        }

    }

    @Builder
    public record OrderSheetItemResponse(
            String orderSheetItemId,
            int quantity,
            ProductInfo product,
            List<OptionInfo> options,
            ItemPriceInfo price,
            ItemCouponInfo coupon
    ) {
        public static OrderSheetItemResponse from(OrderSheetResult.OrderSheetItemResult item) {
            return OrderSheetItemResponse.builder()
                    .orderSheetItemId(item.orderSheetItemId())
                    .quantity(item.quantity())
                    .product(ProductInfo.from(item.product()))
                    .options(OptionInfo.from(item.options()))
                    .price(ItemPriceInfo.from(item.price()))
                    .coupon(ItemCouponInfo.from(item.coupon()))
                    .build();
        }

        public static List<OrderSheetItemResponse> from(List<OrderSheetResult.OrderSheetItemResult> items) {
            return items.stream().map(OrderSheetItemResponse::from).toList();
        }
    }

    @Builder
    public record CartCouponInfo(
            Long cartCouponId,
            String name,
            Long appliedDiscountAmount
    ) {
        public static CartCouponInfo from(OrderSheetResult.AppliedCartCouponResult cartCoupon) {
            return CartCouponInfo.builder()
                    .cartCouponId(cartCoupon.cartCouponId())
                    .name(cartCoupon.name())
                    .appliedDiscountAmount(cartCoupon.appliedDiscountAmount().longValue())
                    .build();
        }
    }

    @Builder
    public record PaymentSummaryInfo(
            Long totalOriginalAmount,
            Long totalItemDiscount,
            Long totalItemCouponDiscount,
            Long cartCouponDiscount,
            Long usedPoints,
            Long totalPaymentAmount
    ) {
        public static PaymentSummaryInfo from(OrderSheetResult.PaymentSummaryResult paymentSummary) {
            return PaymentSummaryInfo.builder()
                    .totalOriginalAmount(paymentSummary.totalOriginalAmount().longValue())
                    .totalItemDiscount(paymentSummary.totalItemDiscount().longValue())
                    .totalItemCouponDiscount(paymentSummary.totalItemCouponDiscount().longValue())
                    .cartCouponDiscount(paymentSummary.cartCouponDiscount().longValue())
                    .usedPoints(paymentSummary.usedPoints().longValue())
                    .totalPaymentAmount(paymentSummary.totalPaymentAmount().longValue())
                    .build();
        }
    }

    @Builder
    public record ProductInfo(
            Long productId,
            Long productVariantId,
            String sku,
            String productName,
            String thumbnail
    ) {
        public static ProductInfo from(ProductSnapshot product) {
            return ProductInfo.builder()
                    .productId(product.getProductId())
                    .productVariantId(product.getProductVariantId())
                    .sku(product.getSku())
                    .productName(product.getProductName())
                    .thumbnail(product.getThumbnail())
                    .build();

        }

    }

    @Builder
    public record OptionInfo(
            String optionTypeName,
            String optionValueName
    ) {

        public static OptionInfo from(ProductOptionSnapshot option) {
            return OptionInfo.builder()
                    .optionTypeName(option.getOptionTypeName())
                    .optionValueName(option.getOptionValueName())
                    .build();
        }

        public static List<OptionInfo> from(List<ProductOptionSnapshot> options) {
            return options.stream().map(OptionInfo::from).toList();
        }

    }

    @Builder
    public record ItemPriceInfo(
            Long unitOriginalPrice,
            Long unitDiscountedPrice,
            Long lineTotal,
            Long finalItemAmount
    ) {
        public static ItemPriceInfo from(OrderSheetResult.ItemPriceResult itemPrice) {
            return ItemPriceInfo.builder()
                    .unitOriginalPrice(itemPrice.unitOriginalPrice().longValue())
                    .unitDiscountedPrice(itemPrice.unitDiscountedPrice().longValue())
                    .lineTotal(itemPrice.lineTotal().longValue())
                    .finalItemAmount(itemPrice.finalAmount().longValue())
                    .build();

        }

    }

    @Builder
    public record ItemCouponInfo(
            Long itemCouponId,
            String name,
            Long appliedDiscountAmount
    ) {
        public static ItemCouponInfo from(OrderSheetResult.AppliedItemCouponResult itemCoupon) {
            return ItemCouponInfo.builder()
                    .itemCouponId(itemCoupon.itemCouponId())
                    .name(itemCoupon.name())
                    .appliedDiscountAmount(itemCoupon.appliedDiscountAmount().longValue())
                    .build();
        }

    }

    @Builder
    public record PointInfo(
            Long availablePoints,
            Long maxUsablePoints
    ) {
        public static PointInfo from(OrderSheetResult.PointResult point) {
            return PointInfo.builder()
                    .availablePoints(point.availablePoints().longValue())
                    .maxUsablePoints(point.maxUsablePoints().longValue())
                    .build();
        }
    }
}
