package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.service.dto.result.CalculatedOrderAmounts;
import com.example.order_service.api.order.domain.service.dto.result.OrderCouponInfo;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductAmount;
import com.example.order_service.api.order.domain.service.dto.result.OrderProductInfo;
import com.example.order_service.api.order.facade.dto.command.CreateOrderItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.api.support.fixture.order.OrderCommandFixture.anOrderItemCommand;
import static com.example.order_service.api.support.fixture.order.OrderCouponFixture.anOrderCouponInfo;
import static com.example.order_service.api.support.fixture.order.OrderPriceFixture.anCalculatedOrderAmounts;
import static com.example.order_service.api.support.fixture.order.OrderPriceFixture.anOrderProductAmount;
import static com.example.order_service.api.support.fixture.order.OrderProductFixture.anOrderProductInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OrderPriceCalculatorTest {

    private final OrderPriceCalculator calculator = new OrderPriceCalculator();

    @Nested
    @DisplayName("주문 상품 가격 계산")
    class CalculateItemAmounts {

        @Test
        @DisplayName("주문 상품의 가격 정보를 계산한다")
        void calculateItemAmounts(){
            //given
            CreateOrderItemCommand itemCommand1 = anOrderItemCommand().productVariantId(1L).quantity(3).build();
            CreateOrderItemCommand itemCommand2 = anOrderItemCommand().productVariantId(2L).quantity(5).build();
            OrderProductInfo product1 = anOrderProductInfo()
                    .productVariantId(1L)
                    .originalPrice(10000L)
                    .discountAmount(1000L)
                    .discountedPrice(9000L)
                    .build();
            OrderProductInfo product2 = anOrderProductInfo()
                    .productVariantId(2L)
                    .originalPrice(20000L)
                    .discountAmount(2000L)
                    .discountedPrice(18000L)
                    .build();
            OrderProductAmount expectedResult = anOrderProductAmount()
                    .totalOriginalAmount(130000L)
                    .totalDiscountAmount(13000L)
                    .subTotalAmount(117000L)
                    .build();
            //when
            OrderProductAmount result = calculator.calculateItemAmounts(List.of(itemCommand1, itemCommand2), List.of(product1, product2));
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult);
        }
    }

    @Nested
    @DisplayName("최종 주문 가격 정보 계산")
    class CalculateFinalPrice {

        @Test
        @DisplayName("최종 주문 가격 정보를 계산한다")
        void calculateFinalPrice(){
            //given
            OrderProductAmount productAmount = anOrderProductAmount().build();
            OrderCouponInfo coupon = anOrderCouponInfo().build();
            CalculatedOrderAmounts expectedResult = anCalculatedOrderAmounts().build();
            //when
            CalculatedOrderAmounts result = calculator.calculateOrderPrice(productAmount, coupon, 1000L, 7000L);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("최종 결제 가격과 예상 결제 가격이 다르면 예외가 발생한다")
        void calculateFinaPrice_expectedPrice_missMatch(){
            //given
            OrderProductAmount productAmount = anOrderProductAmount().build();
            OrderCouponInfo coupon = anOrderCouponInfo().build();
            //when
            //then
            assertThatThrownBy(() -> calculator.calculateOrderPrice(productAmount, coupon, 1000L, 9000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_PRICE_MISMATCH);
        }
    }
}
