package com.example.order_service.api.support.fixture;

import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderDomainServiceTestFixture {
    public static final Long USER_ID = 1L;
    public static final String ADDRESS = "서울시 테헤란로 123";

    public static final Long PROD1_ID = 1L;
    public static final String PROD1_NAME = "상품1";
    public static final Long PROD1_PRICE = 3000L;
    public static final int PROD1_QTY = 3;

    public static final Long PROD2_ID = 2L;
    public static final String PROD2_NAME = "상품2";
    public static final Long PROD2_PRICE = 5000L;
    public static final int PROD2_QTY = 5;

    public static final int DISCOUNT_RATE = 10;
    public static final Long COUPON_DISCOUNT = 1000L;
    public static final Long USE_POINT = 1000L;

    public static final Long PROD1_LINE_TOTAL = (PROD1_PRICE * (100 - DISCOUNT_RATE) / 100) * PROD1_QTY;
    public static final Long PROD2_LINE_TOTAL = (PROD2_PRICE * (100 - DISCOUNT_RATE) / 100) * PROD2_QTY;

    public static final Long TOTAL_ORIGIN_PRICE = (PROD1_PRICE * PROD1_QTY) + (PROD2_PRICE * PROD2_QTY);
    public static final Long TOTAL_PROD_DISCOUNT = TOTAL_ORIGIN_PRICE * DISCOUNT_RATE / 100;
    public static final Long FINAL_PRICE = TOTAL_ORIGIN_PRICE - TOTAL_PROD_DISCOUNT - COUPON_DISCOUNT - USE_POINT;

    public static OrderCreationContext createDefaultContext() {
        OrderProductResponse p1 = createProductResponse(PROD1_ID, PROD1_NAME, PROD1_PRICE, "http://thumbnail1.jpg", Map.of("사이즈", "XL"));
        OrderProductResponse p2 = createProductResponse(PROD2_ID, PROD2_NAME, PROD2_PRICE, "http://thumbnail2.jpg", Map.of("용량", "256GB"));
        List<OrderProductResponse> products = List.of(p1, p2);

        Map<Long, Integer> quantityMap = Map.of(PROD1_ID, PROD1_QTY, PROD2_ID, PROD2_QTY);
        List<OrderItemSpec> specs = createOrderItemSpec(products, quantityMap);
        ItemCalculationResult itemCalc = createItemCalculationResult(quantityMap, products);
        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L).couponName("1000원 할인 쿠폰").discountAmount(1000L).build();

        PriceCalculateResult priceResult = PriceCalculateResult.of(itemCalc, coupon, USE_POINT, FINAL_PRICE);
        return OrderCreationContext.builder()
                .userId(USER_ID)
                .itemSpecs(specs)
                .priceResult(priceResult)
                .deliveryAddress(ADDRESS)
                .build();
    }

    private static ItemCalculationResult createItemCalculationResult(Map<Long, Integer> quantityMap, List<OrderProductResponse> products) {
        Map<Long, OrderProductResponse.UnitPrice> priceMap = products.stream()
                .collect(Collectors.toMap(OrderProductResponse::getProductVariantId, OrderProductResponse::getUnitPrice));
        return ItemCalculationResult.of(quantityMap, priceMap);
    }

    private static List<OrderItemSpec> createOrderItemSpec(List<OrderProductResponse> products, Map<Long, Integer> quantityMap) {
        return products.stream()
                .map(p -> OrderItemSpec.of(p, quantityMap.get(p.getProductVariantId())))
                .toList();
    }

    private static OrderProductResponse createProductResponse(Long id, String productName, Long price, String thumbnailUrl, Map<String, String> options) {
        long discountAmt = price * DISCOUNT_RATE / 100;
        return OrderProductResponse.builder()
                .productId(id)
                .productVariantId(id)
                .productName(productName)
                .thumbnailUrl(thumbnailUrl)
                .unitPrice(OrderProductResponse.UnitPrice.builder()
                        .originalPrice(price)
                        .discountRate(DISCOUNT_RATE)
                        .discountAmount(discountAmt)
                        .discountedPrice(price - discountAmt)
                        .build())
                .itemOptions(options.entrySet().stream()
                        .map(item -> OrderProductResponse.ItemOption.builder()
                                .optionTypeName(item.getKey()).optionValueName(item.getValue()).build()).toList())
                .build();
    }
}
