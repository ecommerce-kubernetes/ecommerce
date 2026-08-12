package com.example.order_service.order.adapter.in.web.dto.order.response;

import com.example.order_service.order.application.service.order.dto.result.OrderSummaryResult;
import com.example.order_service.order.domain.order.OrderItemAmount;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderSummaryResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long orderId,
        String status,
        List<OrderItemResponse> orderItems,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {

    @Builder
    public record OrderItemResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Long orderItemId,
            ProductInfo product,
            List<OptionInfo> options,
            Integer quantity,
            OrderItemAmountResponse orderItemAmount
    ) {

        public static OrderItemResponse from(OrderSummaryResult.OrderItemResult result) {
            return OrderItemResponse.builder()
                    .orderItemId(result.orderItemId())
                    .product(ProductInfo.from(result.product()))
                    .options(OptionInfo.from(result.options()))
                    .quantity(result.quantity())
                    .orderItemAmount(OrderItemAmountResponse.from(result.orderItemAmount()))
                    .build();
        }

        public static List<OrderItemResponse> from(List<OrderSummaryResult.OrderItemResult> results) {
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
        public static OptionInfo from(ProductOptionSnapshot optionSnapshot) {
            return OptionInfo.builder()
                    .optionTypeName(optionSnapshot.getOptionTypeName())
                    .optionValueName(optionSnapshot.getOptionValueName())
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

    public static OrderSummaryResponse from(OrderSummaryResult result) {
        return OrderSummaryResponse.builder()
                .orderId(result.orderId())
                .status(result.status().name())
                .orderItems(OrderItemResponse.from(result.orderItems()))
                .createdAt(result.createdAt())
                .build();
    }
}
