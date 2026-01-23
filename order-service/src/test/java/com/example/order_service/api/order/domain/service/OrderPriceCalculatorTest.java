package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.domain.model.vo.OrderPriceInfo;
import com.example.order_service.api.order.domain.model.vo.PriceCalculateResult;
import com.example.order_service.api.order.domain.service.dto.result.ItemCalculationResult;
import com.example.order_service.api.order.infrastructure.client.coupon.dto.OrderCouponDiscountResponse;
import com.example.order_service.api.order.infrastructure.client.product.dto.OrderProductResponse;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OrderPriceCalculatorTest {

    private final OrderPriceCalculator calculator = new OrderPriceCalculator();

    @Test
    @DisplayName("주문 상품의 가격 정보를 계산한다")
    void calculateItemAmounts() {
        //given
        CreateOrderItemCommand item1 = CreateOrderItemCommand.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        CreateOrderItemCommand item2 = CreateOrderItemCommand.builder()
                .productVariantId(2L)
                .quantity(5)
                .build();
        OrderProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail.jpg", List.of());
        OrderProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10,
                "http://thumbnail.jpg", List.of());

        List<CreateOrderItemCommand> requestItems = List.of(item1, item2);
        List<OrderProductResponse> responseItems = List.of(product1, product2);
        //when
        ItemCalculationResult result = calculator.calculateItemAmounts(requestItems, responseItems);
        //then
        assertThat(result)
                .extracting("totalOriginalPrice", "totalProductDiscount", "subTotalPrice")
                .contains(34000L, 3400L, 30600L);
    }

    @Test
    @DisplayName("쿠폰 할인가격이 적용된 최종 가격 정보를 계산한다")
    void calculateFinalPrice() {
        //given
        OrderUserResponse user = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(4000L)
                .build();

        OrderCouponDiscountResponse coupon = OrderCouponDiscountResponse.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(1000L)
                .build();

        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );

        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);

        //when
        PriceCalculateResult result = calculator.calculateFinalPrice(1000, itemCalculationResult, 28600L, user, coupon);
        //then
        assertThat(result.getOrderPriceInfo())
                .extracting(
                        OrderPriceInfo::getTotalOriginPrice,
                        OrderPriceInfo::getTotalProductDiscount,
                        OrderPriceInfo::getCouponDiscount,
                        OrderPriceInfo::getPointDiscount,
                        OrderPriceInfo::getFinalPaymentAmount)
                .contains(34000L, 3400L, 1000L, 1000L, 28600L);
        assertThat(result.getAppliedCoupon())
                .extracting("couponId", "couponName", "discountAmount")
                .contains(1L, "1000원 할인 쿠폰", 1000L);
    }
    
    @Test
    @DisplayName("쿠폰을 사용하지 않은 최종 가격 정보를 계산한다")
    void calculateFinalPrice_When_Coupon_Not_Used() {
        //given
        OrderUserResponse user = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(4000L)
                .build();

        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );
        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);
        //when
        PriceCalculateResult result = calculator.calculateFinalPrice(1000, itemCalculationResult, 29600L, user, null);
        //then
        assertThat(result.getOrderPriceInfo())
                .extracting(
                        OrderPriceInfo::getTotalOriginPrice,
                        OrderPriceInfo::getTotalProductDiscount,
                        OrderPriceInfo::getCouponDiscount,
                        OrderPriceInfo::getPointDiscount,
                        OrderPriceInfo::getFinalPaymentAmount)
                .contains(34000L, 3400L, 0L, 1000L, 29600L);
        assertThat(result.getAppliedCoupon()).isNull();
    }
    
    @Test
    @DisplayName("최종 결제금액을 계산할때 포인트가 부족한 경우 예외를 던진다")
    void calculateFinalPrice_When_Not_Enough_Point() {
        //given
        OrderUserResponse user = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(100L)
                .build();
        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );
        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);
        //when
        //then
        assertThatThrownBy(() -> calculator.calculateFinalPrice(1000, itemCalculationResult, 29600L, user, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_INSUFFICIENT_POINT_BALANCE);
    }

    @Test
    @DisplayName("최종 결제금액을 계산할때 예상 결제 금액과 최종 주문 가격이 동일하지 않는 경우 예외를 던진다")
    void calculateFinalPrice_When_Not_Equal_FinalPaymentAmount_To_ExpectedPrice() {
        //given
        OrderUserResponse user = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(10000L)
                .build();
        Map<Long, Integer> quantityByVariantId = Map.of(1L, 3, 2L, 5);
        Map<Long, OrderProductResponse.UnitPrice> unitPriceByVariantId = Map.of(
                1L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(3000L)
                        .discountRate(10)
                        .discountAmount(300L)
                        .discountedPrice(2700L).build(),
                2L, OrderProductResponse.UnitPrice.builder()
                        .originalPrice(5000L)
                        .discountRate(10)
                        .discountAmount(500L)
                        .discountedPrice(4500L).build()
        );
        ItemCalculationResult itemCalculationResult = ItemCalculationResult.of(quantityByVariantId, unitPriceByVariantId);
        //when
        //then
        assertThatThrownBy(() -> calculator.calculateFinalPrice(1000, itemCalculationResult, 28600L, user, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_PRICE_MISMATCH);
    }

    private OrderProductResponse createProductResponse(Long productId, Long productVariantId,
                                                       String productName, Long originalPrice, int discountRate,
                                                       String thumbnail, List<OrderProductResponse.ItemOption> options){
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
                .itemOptions(options)
                .build();
    }
}
