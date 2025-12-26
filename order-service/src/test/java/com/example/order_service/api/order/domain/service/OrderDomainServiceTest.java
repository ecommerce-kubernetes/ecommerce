package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.order.domain.model.Order;
import com.example.order_service.api.order.domain.model.OrderFailureCode;
import com.example.order_service.api.order.domain.model.OrderStatus;
import com.example.order_service.api.order.domain.model.vo.AppliedCoupon;
import com.example.order_service.api.order.domain.model.vo.PaymentInfo;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.repository.OrderRepository;
import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.OrderItemSpec;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.domain.service.dto.result.OrderDto;
import com.example.order_service.api.order.domain.service.dto.result.OrderItemDto;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponCalcResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.support.ExcludeInfraTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class OrderDomainServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderDomainService orderDomainService;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("주문을 저장한다")
    void saveOrder(){
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
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, coupon, 1000L, 28600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
        //when
        OrderDto orderDto = orderDomainService.saveOrder(creationContext);
        //then
        assertThat(orderDto.getOrderId()).isNotNull();
        assertThat(orderDto)
                .extracting(OrderDto::getStatus, OrderDto::getOrderName, OrderDto::getOrderFailureCode)
                .contains(OrderStatus.PENDING, "상품1 외 1건", null);
        assertThat(orderDto.getOrderedAt()).isNotNull();

        assertThat(orderDto.getPaymentInfo())
                .extracting(PaymentInfo::getTotalOriginPrice, PaymentInfo::getTotalProductDiscount,
                        PaymentInfo::getCouponDiscount, PaymentInfo::getUsedPoint, PaymentInfo::getFinalPaymentAmount)
                .contains(34000L, 3400L, 1000L, 1000L, 28600L);

        assertThat(orderDto.getOrderItemDtoList()).hasSize(2);

        assertThat(orderDto.getOrderItemDtoList())
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
                                    .extracting(OrderItemDto.UnitPrice::getOriginalPrice, OrderItemDto.UnitPrice::getDiscountRate,
                                            OrderItemDto.UnitPrice::getDiscountAmount, OrderItemDto.UnitPrice::getDiscountedPrice)
                                    .contains(3000L, 10, 300L, 2700L);
                            assertThat(item1.getItemOptionDtos())
                                    .extracting(OrderItemDto.ItemOptionDto::getOptionTypeName, OrderItemDto.ItemOptionDto::getOptionValueName)
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
                                    .extracting(OrderItemDto.UnitPrice::getOriginalPrice, OrderItemDto.UnitPrice::getDiscountRate,
                                            OrderItemDto.UnitPrice::getDiscountAmount, OrderItemDto.UnitPrice::getDiscountedPrice)
                                    .contains(5000L, 10, 500L, 4500L);
                            assertThat(item2.getItemOptionDtos())
                                    .extracting(OrderItemDto.ItemOptionDto::getOptionTypeName, OrderItemDto.ItemOptionDto::getOptionValueName)
                                    .containsExactlyInAnyOrder(
                                            tuple("용량", "256GB")
                                    );
                        }
                );

        assertThat(orderDto.getAppliedCoupon())
                .extracting(AppliedCoupon::getCouponId, AppliedCoupon::getCouponName, AppliedCoupon::getDiscountAmount)
                .contains(1L, "1000원 할인 쿠폰", 1000L);
    }

    @Test
    @DisplayName("주문 상태를 변경한다")
    void changeOrderStatus() {
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
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, coupon, 1000L, 28600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
        Order order = Order.create(creationContext);
        Order save = orderRepository.save(order);
        //when
        OrderDto orderDto = orderDomainService.changeOrderStatus(save.getId(), OrderStatus.PAYMENT_WAITING);
        //then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.PAYMENT_WAITING);
        assertThat(orderDto.getOrderFailureCode()).isNull();
    }

    @Test
    @DisplayName("주문의 상태를 변경할때 주문을 찾을 수 없으면 예외를 던진다")
    void changeOrderStatus_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderDomainService.changeOrderStatus(999L, OrderStatus.PAYMENT_WAITING))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문을 조회한다")
    void getOrder(){
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
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, coupon, 1000L, 28600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
        Order order = Order.create(creationContext);
        Order save = orderRepository.save(order);
        //when
        OrderDto orderDto = orderDomainService.getOrder(save.getId());
        //then
        assertThat(orderDto.getOrderId()).isNotNull();
        assertThat(orderDto)
                .extracting(OrderDto::getStatus, OrderDto::getOrderName, OrderDto::getOrderFailureCode)
                .contains(OrderStatus.PENDING, "상품1 외 1건", null);
        assertThat(orderDto.getOrderedAt()).isNotNull();

        assertThat(orderDto.getPaymentInfo())
                .extracting(PaymentInfo::getTotalOriginPrice, PaymentInfo::getTotalProductDiscount,
                        PaymentInfo::getCouponDiscount, PaymentInfo::getUsedPoint, PaymentInfo::getFinalPaymentAmount)
                .contains(34000L, 3400L, 1000L, 1000L, 28600L);

        assertThat(orderDto.getOrderItemDtoList()).hasSize(2);

        assertThat(orderDto.getOrderItemDtoList())
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
                                    .extracting(OrderItemDto.UnitPrice::getOriginalPrice, OrderItemDto.UnitPrice::getDiscountRate,
                                            OrderItemDto.UnitPrice::getDiscountAmount, OrderItemDto.UnitPrice::getDiscountedPrice)
                                    .contains(3000L, 10, 300L, 2700L);
                            assertThat(item1.getItemOptionDtos())
                                    .extracting(OrderItemDto.ItemOptionDto::getOptionTypeName, OrderItemDto.ItemOptionDto::getOptionValueName)
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
                                    .extracting(OrderItemDto.UnitPrice::getOriginalPrice, OrderItemDto.UnitPrice::getDiscountRate,
                                            OrderItemDto.UnitPrice::getDiscountAmount, OrderItemDto.UnitPrice::getDiscountedPrice)
                                    .contains(5000L, 10, 500L, 4500L);
                            assertThat(item2.getItemOptionDtos())
                                    .extracting(OrderItemDto.ItemOptionDto::getOptionTypeName, OrderItemDto.ItemOptionDto::getOptionValueName)
                                    .containsExactlyInAnyOrder(
                                            tuple("용량", "256GB")
                                    );
                        }
                );

        assertThat(orderDto.getAppliedCoupon())
                .extracting(AppliedCoupon::getCouponId, AppliedCoupon::getCouponName, AppliedCoupon::getDiscountAmount)
                .contains(1L, "1000원 할인 쿠폰", 1000L);
    }

    @Test
    @DisplayName("주문을 조회할때 주문을 찾을 수 없으면 예외를 던진다")
    void getOrder_notFound(){
        //given
        //when
        //then
        assertThatThrownBy(() -> orderDomainService.getOrder(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문을 실패 상태로 변경한다")
    void changeCanceled() {
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
        PriceCalculateResult priceCalculateResult = PriceCalculateResult.of(itemCalculationResult, coupon, 1000L, 28600L);
        OrderCreationContext creationContext = OrderCreationContext.builder()
                .userId(1L)
                .itemSpecs(orderItemSpec)
                .priceResult(priceCalculateResult)
                .deliveryAddress("서울시 테헤란로 123")
                .build();
        Order order = Order.create(creationContext);
        Order save = orderRepository.save(order);
        //when
        OrderDto orderDto = orderDomainService.changeCanceled(save.getId(), OrderFailureCode.OUT_OF_STOCK);
        //then
        assertThat(orderDto.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(save.getFailureCode()).isEqualTo(OrderFailureCode.OUT_OF_STOCK);
    }

    @Test
    @DisplayName("주문을 실패 상태로 변경할때 주문을 찾을 수 없으면 예외를 던진다")
    void changeCanceled_notFound() {
        //given
        //when
        //then
        assertThatThrownBy(() -> orderDomainService.changeCanceled(999L, OrderFailureCode.OUT_OF_STOCK))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다");
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

    private ItemCalculationResult createItemCalculationResult(Map<Long, Integer> quantityById, List<OrderProductResponse> products) {
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = products.stream().collect(Collectors.toMap(OrderProductResponse::getProductVariantId, OrderProductResponse::getUnitPrice));
        return ItemCalculationResult.of(quantityById, unitPriceByVariantId);
    }
}
