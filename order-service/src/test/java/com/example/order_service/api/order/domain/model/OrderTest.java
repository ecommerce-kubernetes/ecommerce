package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest {

    @Test
    @DisplayName("주문 생성시 쿠폰이 있는 경우 쿠폰 정보가 매핑된 주문을 생성한다")
    void createOrder(){
        //given
        OrderItemSpec orderItem1 = createOrderItemSpec(1L, 1L, "상품1", "http://thumbnail.jpg", 3000L, 10,
                3, Map.of("사이즈", "XL"));
        OrderItemSpec orderItem2 = createOrderItemSpec(2L, 2L, "상품2", "http://thumbnail2.jpg", 5000L, 10, 5,
                Map.of("용량", "256GB"));
        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        PriceCalculateResult priceCalculateResult = createPriceCalculateResult(List.of(orderItem1, orderItem2), coupon, 1000L);
        OrderCreationContext orderCreationContext = createOrderCreationContext(1L, List.of(orderItem1, orderItem2), priceCalculateResult, "서울시 테헤란로 123");
        //when
        Order order = Order.create(orderCreationContext);
        //then
        assertThat(order).
                extracting("userId", "status", "orderName", "deliveryAddress", "totalOriginPrice", "totalProductDiscount", "couponDiscount",
                        "pointDiscount", "finalPaymentAmount")
                .contains(1L, OrderStatus.PENDING, "상품1 외 1건", "서울시 테헤란로 123", 34000L, 3400L, 1000L, 1000L, 28600L);

        assertThat(order.getCoupon()).isNotNull();
        assertThat(order.getCoupon().getOrder()).isEqualTo(order);
        assertThat(order.getCoupon())
                .extracting("couponId", "couponName", "discountAmount")
                .contains(1L, "1000원 할인 쿠폰", 1000L);

        assertThat(order.getOrderItems())
                .allSatisfy(orderItem -> assertThat(orderItem.getOrder()).isEqualTo(order));
    }

    @Test
    @DisplayName("주문 생성시 쿠폰이 없는 경우 쿠폰 정보가 매핑되지 않은 주문을 생성한다")
    void createOrder_Without_Coupon(){
        //given
        OrderItemSpec orderItem1 = createOrderItemSpec(1L, 1L, "상품1", "http://thumbnail.jpg", 3000L, 10,
                3, Map.of("사이즈", "XL"));
        OrderItemSpec orderItem2 = createOrderItemSpec(2L, 2L, "상품2", "http://thumbnail2.jpg", 5000L, 10, 5,
                Map.of("용량", "256GB"));
        PriceCalculateResult priceCalculateResult = createPriceCalculateResult(List.of(orderItem1, orderItem2), null, 1000L);
        OrderCreationContext orderCreationContext = createOrderCreationContext(1L, List.of(orderItem1, orderItem2), priceCalculateResult, "서울시 테헤란로 123");
        //when
        Order order = Order.create(orderCreationContext);
        //then
        assertThat(order).
                extracting("userId", "status", "orderName", "deliveryAddress", "totalOriginPrice", "totalProductDiscount", "couponDiscount",
                        "pointDiscount", "finalPaymentAmount")
                .contains(1L, OrderStatus.PENDING, "상품1 외 1건", "서울시 테헤란로 123", 34000L, 3400L, 0L, 1000L, 29600L);
        assertThat(order.getCoupon()).isNull();


        assertThat(order.getOrderItems())
                .allSatisfy(orderItem -> assertThat(orderItem.getOrder()).isEqualTo(order));
    }

    private PriceCalculateResult createPriceCalculateResult(List<OrderItemSpec> orderItemSpecs, OrderCouponCalcResponse coupon,
                                                            Long usedPoint){
        long totalOriginPrice = orderItemSpecs.stream()
                .mapToLong(item -> item.getUnitPrice().getOriginalPrice() * item.getQuantity()).sum();
        long totalProductDiscount = orderItemSpecs.stream()
                .mapToLong(item -> item.getUnitPrice().getDiscountAmount() * item.getQuantity()).sum();
        long finalPaymentAmount = totalOriginPrice - totalProductDiscount - (coupon != null ? coupon.getDiscountAmount() : 0L) - usedPoint;

        ItemCalculationResult itemCalcResult = ItemCalculationResult.builder()
                .totalOriginalPrice(totalOriginPrice)
                .totalProductDiscount(totalProductDiscount)
                .subTotalPrice(totalOriginPrice - totalProductDiscount)
                .build();

        return PriceCalculateResult.of(itemCalcResult, coupon, usedPoint, finalPaymentAmount);
    }

    private OrderCreationContext createOrderCreationContext(Long userId, List<OrderItemSpec> itemSpecs, PriceCalculateResult priceCalculateResult,
                                                            String deliveryAddress) {
        return OrderCreationContext.builder()
                .userId(userId)
                .itemSpecs(itemSpecs)
                .priceResult(priceCalculateResult)
                .deliveryAddress(deliveryAddress)
                .build();
    }

    private OrderItemSpec createOrderItemSpec(Long productId, Long productVariantId, String productName, String thumbnail,
                                              long originalPrice, int discountRate, int quantity, Map<String, String> optionMap){

        long discountAmount = originalPrice * discountRate / 100;
        return OrderItemSpec.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .thumbnailUrl(thumbnail)
                .unitPrice(
                        OrderItemSpec.UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originalPrice - discountAmount)
                                .build())
                .lineTotal((originalPrice - discountAmount) * quantity)
                .quantity(quantity)
                .itemOptions(
                        optionMap.entrySet().stream().map(entry -> OrderItemSpec.ItemOption.builder()
                                .optionTypeName(entry.getKey())
                                .optionValueName(entry.getValue()).build()).toList())
                .build();
    }
}
