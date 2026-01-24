package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.service.dto.result.CalculatedOrderAmounts;
import com.example.order_service.api.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OrderPriceCalculatorTest {

    private final OrderPriceCalculator calculator = new OrderPriceCalculator();

    private CreateOrderItemCommand mockItemCommand(Long variantId, Integer quantity) {
        return CreateOrderItemCommand.builder()
                .productVariantId(variantId)
                .quantity(quantity)
                .build();
    }

    private OrderProductAmount mockOrderProductAmount(Long totalOriginalAmount, Long totalDiscountAmount) {
        return OrderProductAmount.builder()
                .totalOriginalAmount(totalOriginalAmount)
                .totalDiscountAmount(totalDiscountAmount)
                .subTotalAmount(totalOriginalAmount - totalDiscountAmount)
                .build();
    }

    private OrderProductInfo mockOrderProductInfo(Long variantId, Long originalPrice, Integer discountRate) {
        long discountAmount = (long) (originalPrice * (discountRate / 100.0));
        return OrderProductInfo.builder()
                .productId(1L)
                .productVariantId(variantId)
                .sku("TEST")
                .productName("상품")
                .originalPrice(originalPrice)
                .discountRate(discountRate)
                .discountAmount(discountAmount)
                .discountedPrice(originalPrice - discountAmount)
                .build();
    }

    private OrderCouponInfo mockCouponInfo(Long discountAmount) {
        return OrderCouponInfo.builder()
                .couponId(1L)
                .couponName("1000원 할인 쿠폰")
                .discountAmount(discountAmount)
                .build();
    }

    @Nested
    @DisplayName("주문 상품 가격 계산")
    class CalculateItemAmounts {

        @Test
        @DisplayName("주문 상품의 가격 정보를 계산한다")
        void calculateItemAmounts(){
            //given
            CreateOrderItemCommand itemCommand1 = mockItemCommand(1L, 3);
            CreateOrderItemCommand itemCommand2 = mockItemCommand(2L, 5);
            OrderProductInfo product1 = mockOrderProductInfo(1L, 10000L, 10);
            OrderProductInfo product2 = mockOrderProductInfo(2L, 20000L, 20);
            //when
            OrderProductAmount result = calculator.calculateItemAmounts(List.of(itemCommand1, itemCommand2), List.of(product1, product2));
            //then
            assertThat(result)
                    .extracting(OrderProductAmount::getTotalOriginalAmount, OrderProductAmount::getTotalDiscountAmount, OrderProductAmount::getSubTotalAmount)
                    .containsExactly(130000L, 23000L, 107000L);
        }
    }

    @Nested
    @DisplayName("최종 주문 가격 정보 계산")
    class CalculateFinalPrice {

        @Test
        @DisplayName("최종 주문 가격 정보를 계산한다")
        void calculateFinalPrice(){
            //given
            OrderProductAmount productAmount = mockOrderProductAmount(11000L, 1000L);
            OrderCouponInfo coupon = mockCouponInfo(1000L);
            //when
            CalculatedOrderAmounts result = calculator.calculateOrderPrice(productAmount, coupon, 1000L, 8000L);
            //then
            assertThat(result)
                    .extracting(CalculatedOrderAmounts::getTotalOriginalAmount, CalculatedOrderAmounts::getTotalProductDiscount,
                            CalculatedOrderAmounts::getCouponDiscountAmount, CalculatedOrderAmounts::getUsePointAmount, CalculatedOrderAmounts::getFinalPaymentAmount)
                    .containsExactly(11000L, 1000L, 1000L, 1000L, 8000L);
        }

        @Test
        @DisplayName("최종 결제 가격과 예상 결제 가격이 다르면 예외가 발생한다")
        void calculateFinaPrice_expectedPrice_missMatch(){
            //given
            OrderProductAmount productAmount = mockOrderProductAmount(11000L, 1000L);
            OrderCouponInfo coupon = mockCouponInfo(1000L);
            //when
            //then
            assertThatThrownBy(() -> calculator.calculateOrderPrice(productAmount, coupon, 1000L, 9000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }
    }
}
