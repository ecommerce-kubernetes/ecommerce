package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderAmountTest {

    @Test
    @DisplayName("주문 가격 정보를 생성한다.")
    void of(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemDiscount = Money.wons(3000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(24000L);
        //when
        OrderAmount orderAmount = OrderAmount.of(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, usedPoints, totalPaymentAmount);
        //then
        assertThat(orderAmount)
                .extracting("totalOriginalAmount", "totalItemDiscount", "totalItemCouponDiscount", "cartCouponDiscount", "usedPoints", "totalPaymentAmount")
                .containsExactly(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, usedPoints, totalPaymentAmount);
    }

    @Test
    @DisplayName("총 주문 상품 원 가격이 누락되면 예외가 발생한다")
    void of_totalOriginalAmount_null(){
        //given
        Money totalItemDiscount = Money.wons(3000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(24000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(null, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, usedPoints, totalPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("총 주문 상품 원 가격은 필수 입니다.");
    }

    @Test
    @DisplayName("총 상품 할인 가격이 누락되면 예외가 발생한다.")
    void of_totalItemDiscount_null(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(24000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(totalOriginalAmount, null, totalItemCouponDiscount, cartCouponDiscount, usedPoints, totalPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("총 상품 할인 가격은 필수 입니다.");
    }

    @Test
    @DisplayName("총 상품 쿠폰 할인 가격이 누락되면 예외가 발생한다.")
    void of_totalItemCouponDiscount_null(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemDiscount = Money.wons(3000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(24000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(totalOriginalAmount, totalItemDiscount, null, cartCouponDiscount, usedPoints, totalPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("총 상품 쿠폰 할인 가격은 필수 입니다.");
    }

    @Test
    @DisplayName("장바구니 쿠폰 할인 가격이 누락되면 예외가 발생한다.")
    void of_cartCouponDiscount_null(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemDiscount = Money.wons(3000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(24000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, null, usedPoints, totalPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("장바구니 쿠폰 할인 가격은 필수 입니다.");
    }

    @Test
    @DisplayName("적용 포인트가 누락되면 예외가 발생한다.")
    void of_usedPoints_null(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemDiscount = Money.wons(3000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(24000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, null, totalPaymentAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("적용 포인트는 필수 입니다.");
    }

    @Test
    @DisplayName("최종 결제 금액이 누락되면 예외가 발생한다.")
    void of_totalPaymentAmount_null(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemDiscount = Money.wons(3000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, usedPoints, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("최종 결제 금액은 필수 입니다.");
    }

    @Test
    @DisplayName("총 할인 가격이 주문 원가를 초과하면 예외가 발생한다.")
    void of_orderDiscount_exceeds_total_amount(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemDiscount = Money.wons(30000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(24000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, usedPoints, totalPaymentAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ORDER_DISCOUNT_EXCEEDS_TOTAL_AMOUNT);
    }

    @Test
    @DisplayName("최종 결제 금액이 유효하지 않으면 예외가 발생한다.")
    void of_invalid_totalPaymentAmount(){
        //given
        Money totalOriginalAmount = Money.wons(30000L);
        Money totalItemDiscount = Money.wons(3000L);
        Money totalItemCouponDiscount = Money.wons(1000L);
        Money cartCouponDiscount = Money.wons(1000L);
        Money usedPoints = Money.wons(1000L);
        Money totalPaymentAmount = Money.wons(23000L);
        //when
        //then
        assertThatThrownBy(() -> OrderAmount.of(totalOriginalAmount, totalItemDiscount, totalItemCouponDiscount, cartCouponDiscount, usedPoints, totalPaymentAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_TOTAL_PAYMENT_AMOUNT);
    }
}