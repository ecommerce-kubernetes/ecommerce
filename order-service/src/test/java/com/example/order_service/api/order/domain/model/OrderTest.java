package com.example.order_service.api.order.domain.model;

import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderTest {

    @Test
    @DisplayName("주문 생성시 쿠폰이 있는 경우 쿠폰 정보가 매핑된 주문을 생성한다")
    void createOrder(){
        //given
        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10, "http://thumbnail1.jpg",
                Map.of("사이즈", "XL"));
        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10, "http://thumbnail2.jpg",
                Map.of("용량", "256GB"));
        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        List<OrderItemSpec> orderItemSpec = createOrderItemSpec(List.of(product1, product2), Map.of(1L, 3, 2L, 5));
        ItemCalculationResult itemCalculationResult = createItemCalculationResult(Map.of(1L, 3, 2L, 5), List.of(product1, product2));
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, coupon, coupon.getDiscountAmount(), 1000L, 28600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();

        //when
        Order order = Order.create(creationContext);
        //then
        assertThat(order)
                .extracting("userId", "status", "orderName", "deliveryAddress", "totalOriginPrice", "totalProductDiscount", "couponDiscount",
                        "pointDiscount", "finalPaymentAmount", "failureCode")
                .contains(1L, OrderStatus.PENDING, "상품1 외 1건", "서울시 테헤란로 123", 34000L, 3400L, 1000L, 1000L, 28600L, null);

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
        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10, "http://thumbnail1.jpg",
                Map.of("사이즈", "XL"));
        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10, "http://thumbnail2.jpg",
                Map.of("용량", "256GB"));
        List<OrderItemSpec> orderItemSpec = createOrderItemSpec(List.of(product1, product2), Map.of(1L, 3, 2L, 5));
        ItemCalculationResult itemCalculationResult = createItemCalculationResult(Map.of(1L, 3, 2L, 5), List.of(product1, product2));
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, null, 0L, 1000L, 29600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
        //when
        Order order = Order.create(creationContext);
        //then
        assertThat(order).
                extracting("userId", "status", "orderName", "deliveryAddress", "totalOriginPrice", "totalProductDiscount", "couponDiscount",
                        "pointDiscount", "finalPaymentAmount", "failureCode")
                .containsExactly(1L, OrderStatus.PENDING, "상품1 외 1건", "서울시 테헤란로 123", 34000L, 3400L, 0L, 1000L, 29600L, null);
        assertThat(order.getCoupon()).isNull();


        assertThat(order.getOrderItems())
                .allSatisfy(orderItem -> assertThat(orderItem.getOrder()).isEqualTo(order));
    }

    @Test
    @DisplayName("주문의 상태를 변경한다")
    void changeStatus() {
        //given
        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10, "http://thumbnail1.jpg",
                Map.of("사이즈", "XL"));
        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10, "http://thumbnail2.jpg",
                Map.of("용량", "256GB"));
        List<OrderItemSpec> orderItemSpec = createOrderItemSpec(List.of(product1, product2), Map.of(1L, 3, 2L, 5));
        ItemCalculationResult itemCalculationResult = createItemCalculationResult(Map.of(1L, 3, 2L, 5), List.of(product1, product2));
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, null, 0L, 1000L, 29600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
        Order order = Order.create(creationContext);
        //when
        order.changeStatus(OrderStatus.PAYMENT_WAITING);
        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
    }

    @Test
    @DisplayName("주문을 취소한다")
    void canceled() {
        //given
        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10, "http://thumbnail1.jpg",
                Map.of("사이즈", "XL"));
        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10, "http://thumbnail2.jpg",
                Map.of("용량", "256GB"));
        List<OrderItemSpec> orderItemSpec = createOrderItemSpec(List.of(product1, product2), Map.of(1L, 3, 2L, 5));
        ItemCalculationResult itemCalculationResult = createItemCalculationResult(Map.of(1L, 3, 2L, 5), List.of(product1, product2));
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, null, 0L, 1000L, 29600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
        Order order = Order.create(creationContext);
        //when
        order.canceled(OrderFailureCode.OUT_OF_STOCK);
        //then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getFailureCode()).isEqualTo(OrderFailureCode.OUT_OF_STOCK);
    }

    private ItemCalculationResult createItemCalculationResult(Map<Long, Integer> quantityById, List<OrderProductResponse> products) {
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = products.stream().collect(Collectors.toMap(OrderProductResponse::getProductVariantId, OrderProductResponse::getUnitPrice));
        return ItemCalculationResult.of(quantityById, unitPriceByVariantId);
    }

    private List<OrderItemSpec> createOrderItemSpec(List<OrderProductResponse> products, Map<Long, Integer> quantityByVariantId) {
        return products.stream()
                .map(product -> {
                    int quantity = quantityByVariantId.get(product.getProductVariantId());
                    return OrderItemSpec.of(product, quantity);
                })
                .toList();
    }

    private OrderProductResponse createProductResponse(Long productId, Long productVariantId,
                                                       String productName, Long originalPrice, int discountRate,
                                                       String thumbnail, Map<String, String> options){
        long discountAmount = originalPrice * discountRate / 100;
        return OrderProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .unitPrice(
                        OrderProductResponse.UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originalPrice - discountAmount)
                                .build())
                .thumbnailUrl(thumbnail)
                .itemOptions(
                        options.entrySet().stream().map(entry ->
                                OrderProductResponse.ItemOption.builder().optionTypeName(entry.getKey()).optionValueName(entry.getValue())
                                        .build())
                                .toList()
                )
                .build();
    }
}
