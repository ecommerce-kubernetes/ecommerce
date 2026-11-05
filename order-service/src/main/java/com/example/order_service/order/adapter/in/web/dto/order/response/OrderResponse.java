package com.example.order_service.order.adapter.in.web.dto.order.response;

import com.example.order_service.order.application.service.order.dto.result.OrderResult;
import com.example.order_service.order.domain.order.OrderAmount;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.domain.vo.ShippingAddress;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long orderId,
        String status,
        String orderName,
        OrdererResponse orderer,
        ShippingAddressResponse shippingAddress,
        List<OrderItemResponse> orderItems,
        OrderAmountResponse orderAmount,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {

    @Builder
    public record OrdererResponse(
            Long userId,
            String userName,
            String phoneNumber
    ) {
        public static OrdererResponse from(Orderer orderer) {
            return OrdererResponse.builder()
                    .userId(orderer.getUserId())
                    .userName(orderer.getUserName())
                    .phoneNumber(orderer.getPhoneNumber())
                    .build();
        }
    }

    @Builder
    public record ShippingAddressResponse(
            String receiverName,
            String receiverPhone,
            String zipCode,
            String address,
            String addressDetail
    ) {
        public static ShippingAddressResponse from(ShippingAddress shippingAddress) {
            return ShippingAddressResponse.builder()
                    .receiverName(shippingAddress.getReceiverName())
                    .receiverPhone(shippingAddress.getReceiverPhone())
                    .zipCode(shippingAddress.getZipCode())
                    .address(shippingAddress.getAddress())
                    .addressDetail(shippingAddress.getAddressDetail())
                    .build();
        }
    }

    @Builder
    public record OrderItemResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Long orderItemId,
            ProductInfo product,
            List<OptionInfo> options,
            Integer quantity,
            OrderItemAmountResponse orderItemAmount
    ) {
        public static OrderItemResponse from(OrderResult.OrderItemResult result) {
            ProductInfo product = ProductInfo.from(result.product());
            return OrderItemResponse.builder()
                    .orderItemId(result.orderItemId())
                    .product(product)
                    .options(OptionInfo.from(result.options()))
                    .quantity(result.quantity())
                    .orderItemAmount(OrderItemAmountResponse.from(result.orderItemAmount()))
                    .build();
        }

        public static List<OrderItemResponse> from(List<OrderResult.OrderItemResult> results) {
            return results.stream().map(OrderItemResponse::from).toList();
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
        public static ProductInfo from(ProductSnapshot productSnapshot) {
            return ProductInfo.builder()
                    .productId(productSnapshot.getProductId())
                    .productVariantId(productSnapshot.getProductVariantId())
                    .sku(productSnapshot.getSku())
                    .productName(productSnapshot.getProductName())
                    .thumbnail(productSnapshot.getThumbnail())
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
    public record OrderItemAmountResponse(
            Long originalAmount,
            Long itemDiscount,
            Long lineTotal,
            Long couponDiscount,
            Long finalItemAmount
    ) {
        public static OrderItemAmountResponse from(OrderItemAmount orderItemAmount) {
            return OrderItemAmountResponse.builder()
                    .originalAmount(orderItemAmount.getOriginalAmount().longValue())
                    .itemDiscount(orderItemAmount.getItemDiscount().longValue())
                    .lineTotal(orderItemAmount.getLineTotal().longValue())
                    .couponDiscount(orderItemAmount.getItemCouponDiscount().longValue())
                    .finalItemAmount(orderItemAmount.getFinalAmount().longValue())
                    .build();
        }
    }

    @Builder
    public record OrderAmountResponse(
            Long totalOriginalAmount,
            Long totalItemDiscount,
            Long totalItemCouponDiscount,
            Long cartCouponDiscount,
            Long usedPoints,
            Long totalPaymentAmount
    ) {
        public static OrderAmountResponse from(OrderAmount orderAmount) {
            return OrderAmountResponse.builder()
                    .totalOriginalAmount(orderAmount.getTotalOriginalAmount().longValue())
                    .totalItemDiscount(orderAmount.getTotalItemDiscount().longValue())
                    .totalItemCouponDiscount(orderAmount.getTotalItemCouponDiscount().longValue())
                    .cartCouponDiscount(orderAmount.getCartCouponDiscount().longValue())
                    .usedPoints(orderAmount.getUsedPoints().longValue())
                    .totalPaymentAmount(orderAmount.getTotalPaymentAmount().longValue())
                    .build();
        }
    }

    public static OrderResponse from(OrderResult result) {
        OrdererResponse orderer = OrdererResponse.from(result.orderer());
        ShippingAddressResponse shippingAddress = ShippingAddressResponse.from(result.shippingAddress());
        OrderAmountResponse orderAmountResponse = OrderAmountResponse.from(result.orderAmount());
        return OrderResponse.builder()
                .orderId(result.orderId())
                .status(result.status().name())
                .orderName(result.orderName())
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .orderItems(OrderItemResponse.from(result.orderItems()))
                .orderAmount(orderAmountResponse)
                .createdAt(result.createdAt())
                .build();
    }
}
