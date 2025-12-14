package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderCreationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class OrderDomainServiceTest extends ExcludeInfraServiceTest {

    @Autowired
    private OrderDomainService orderDomainService;

    @Test
    @DisplayName("주문을 저장한다")
    @Transactional
    void saveOrder(){
        //given
        String deliveryAddress = "서울시 테헤란로 123";
        OrderItemSpec orderItem1 = createOrderItemSpec(1L, 1L, "상품1", "http://thumbnail1.jpg", 3000L, 10, 3,
                Map.of("사이즈", "XL"));
        OrderItemSpec orderItem2 = createOrderItemSpec(2L, 2L, "상품2", "http://thumbnail2.jpg", 5000L, 10, 5,
                Map.of("용량", "256GB"));
        OrderCouponCalcResponse coupon = OrderCouponCalcResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();
        PriceCalculateResult priceCalculateResult = createPriceCalculateResult(List.of(orderItem1, orderItem2), coupon, 1000L);
        OrderCreationContext context = createOrderCreationContext(1L, List.of(orderItem1, orderItem2), priceCalculateResult, deliveryAddress);
        //when
        OrderCreationResult orderCreationResult = orderDomainService.saveOrder(context);
        //then
        assertThat(orderCreationResult.getOrderId()).isNotNull();
        assertThat(orderCreationResult)
                .extracting("status", "orderName")
                .contains("PENDING", "상품1 외 1건");
        assertThat(orderCreationResult.getOrderedAt()).isNotNull();

        assertThat(orderCreationResult.getPaymentInfo())
                .extracting("totalOriginPrice", "totalProductDiscount", "couponDiscount", "usedPoint", "finalPaymentAmount")
                .contains(34000L, 3400L, 1000L, 1000L, 28600L);

        assertThat(orderCreationResult.getOrderItemDtoList()).hasSize(2);

        assertThat(orderCreationResult.getOrderItemDtoList())
                .satisfiesExactlyInAnyOrder(
                        item1 -> {
                            assertThat(item1.getOrderItemId()).isNotNull();
                            assertThat(item1.getProductId()).isEqualTo(1L);
                            assertThat(item1.getProductVariantId()).isEqualTo(1L);
                            assertThat(item1.getProductName()).isEqualTo("상품1");
                            assertThat(item1.getThumbnailUrl()).isEqualTo("http://thumbnail1.jpg");
                            assertThat(item1.getQuantity()).isEqualTo(3);
                            assertThat(item1.getLineTotal()).isEqualTo(8100L);
                            assertThat(item1.getUnitPrice())
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .contains(3000L, 10, 300L, 2700L);
                            assertThat(item1.getItemOptionDtos())
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactlyInAnyOrder(
                                            tuple("사이즈", "XL")
                                    );
                        },
                        item2 -> {
                            assertThat(item2.getOrderItemId()).isNotNull();
                            assertThat(item2.getProductId()).isEqualTo(2L);
                            assertThat(item2.getProductVariantId()).isEqualTo(2L);
                            assertThat(item2.getProductName()).isEqualTo("상품2");
                            assertThat(item2.getThumbnailUrl()).isEqualTo("http://thumbnail2.jpg");
                            assertThat(item2.getQuantity()).isEqualTo(5);
                            assertThat(item2.getLineTotal()).isEqualTo(22500L);
                            assertThat(item2.getUnitPrice())
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .contains(5000L, 10, 500L, 4500L);
                            assertThat(item2.getItemOptionDtos())
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactlyInAnyOrder(
                                            tuple("용량", "256GB")
                                    );
                        }
                );

        assertThat(orderCreationResult.getAppliedCoupon())
                .extracting("couponId", "couponName", "discountAmount")
                .contains(1L, "1000원 할인 쿠폰", 1000L);
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
                .quantity(quantity)
                .lineTotal((originalPrice - discountAmount) * quantity)
                .itemOptions(
                        optionMap.entrySet().stream().map(entry -> OrderItemSpec.ItemOption.builder()
                                .optionTypeName(entry.getKey())
                                .optionValueName(entry.getValue()).build()).toList())
                .build();
    }
}
