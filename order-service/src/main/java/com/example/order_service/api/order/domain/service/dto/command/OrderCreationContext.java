package com.example.order_service.api.order.domain.service.dto.command;

import com.example.order_service.api.order.application.dto.command.CreateOrderItemDto;
import com.example.order_service.api.order.domain.service.dto.result.PriceCalculateResult;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class OrderCreationContext {
    private Long userId;
    private List<OrderItemSpec> itemSpecs;
    private CouponSpec couponSpec;
    private Long useToPoint;
    private String deliveryAddress;
    private Long finalPaymentAmount;

    @Builder
    private OrderCreationContext(Long userId, List<OrderItemSpec> itemSpecs, CouponSpec couponSpec,
                                 Long useToPoint, String deliveryAddress, Long finalPaymentAmount){
        this.userId = userId;
        this.itemSpecs = itemSpecs;
        this.couponSpec = couponSpec;
        this.useToPoint = useToPoint;
        this.deliveryAddress = deliveryAddress;
        this.finalPaymentAmount = finalPaymentAmount;
    }

    public static OrderCreationContext of(OrderUserResponse user,
                                          List<CreateOrderItemDto> orderItemDtoList,
                                          List<OrderProductResponse> products,
                                          PriceCalculateResult priceResult, String address){
        Map<Long, Integer> requestMap = orderItemDtoList.stream()
                .collect(Collectors.toMap(CreateOrderItemDto::getProductVariantId, CreateOrderItemDto::getQuantity));

        Map<Long, OrderProductResponse> responseMap = products.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, Function.identity()));

        List<OrderItemSpec> itemSpecs = requestMap.entrySet().stream()
                .map(entry -> {
                    OrderProductResponse orderProduct = responseMap.get(entry.getKey());
                    return OrderItemSpec.of(orderProduct, entry.getValue());
                }).toList();

        CouponSpec couponSpec = Optional.ofNullable(priceResult.getCoupon())
                .map(coupon -> CouponSpec.of(coupon.getCouponId(),
                        coupon.getCouponName(), coupon.getDiscountAmount()))
                .orElse(null);
        return of(user.getUserId(), itemSpecs, couponSpec, priceResult.getUseToPoint(), address, priceResult.getFinalPaymentAmount());
    }

    public static OrderCreationContext of(Long userId, List<OrderItemSpec> itemSpecs, CouponSpec couponSpec, Long useToPoint,
                                          String deliveryAddress, Long finalPaymentAmount){
        return OrderCreationContext.builder()
                .userId(userId)
                .itemSpecs(itemSpecs)
                .couponSpec(couponSpec)
                .useToPoint(useToPoint)
                .deliveryAddress(deliveryAddress)
                .finalPaymentAmount(finalPaymentAmount)
                .build();
    }
}
