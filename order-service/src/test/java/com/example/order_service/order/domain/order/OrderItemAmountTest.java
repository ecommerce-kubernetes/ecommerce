package com.example.order_service.order.domain.order;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemAmountTest {

    @Test
    @DisplayName("주문 항목 가격 정보를 생성한다.")
    void of(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(3000L);
        Money lineTotal = Money.wons(27000L);
        Money itemCouponDiscount = Money.wons(1000L);
        Money finalAmount = Money.wons(26000L);
        //when
        OrderItemAmount orderItemAmount = OrderItemAmount.of(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, finalAmount);
        //then
        assertThat(orderItemAmount)
                .extracting("originalAmount", "itemDiscount", "lineTotal", "itemCouponDiscount", "finalAmount")
                .containsExactly(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, finalAmount);
    }

    @Test
    @DisplayName("주문 항목 가격 정보 생성시 상품 원가격 총액이 누락되면 예외가 발생한다.")
    void of_originalAmount_null(){
        //given
        Money itemDiscount = Money.wons(3000L);
        Money lineTotal = Money.wons(27000L);
        Money itemCouponDiscount = Money.wons(1000L);
        Money finalAmount = Money.wons(26000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(null, itemDiscount, lineTotal, itemCouponDiscount, finalAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("항목 원가 총액은 필수 입니다.");
    }

    @Test
    @DisplayName("주문 항목 가격 정보 생성시 상품 할인 금액 총액이 누락되면 예외가 발생한다.")
    void of_itemDiscount_null(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money lineTotal = Money.wons(27000L);
        Money itemCouponDiscount = Money.wons(1000L);
        Money finalAmount = Money.wons(26000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, null, lineTotal, itemCouponDiscount, finalAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("항목 상품 할인 총액은 필수 입니다.");
    }

    @Test
    @DisplayName("주문 항목 가격 정보 생성시 상품 판매가 총액이 누락되면 예외가 발생한다.")
    void of_lineTotal_null(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(3000L);
        Money itemCouponDiscount = Money.wons(1000L);
        Money finalAmount = Money.wons(26000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, itemDiscount, null, itemCouponDiscount, finalAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상품 판매가 총액은 필수 입니다.");
    }

    @Test
    @DisplayName("주문 항목 가격 정보 생성시 상품 쿠폰 할인 금액이 누락되면 예외가 발생한다.")
    void of_itemCouponDiscount_null(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(3000L);
        Money lineTotal = Money.wons(27000L);
        Money finalAmount = Money.wons(26000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, itemDiscount, lineTotal, null, finalAmount))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("항목 상품 쿠폰 할인 금액은 필수 입니다.");
    }

    @Test
    @DisplayName("주문 항목 최종 금액이 누락되면 예외가 발생한다.")
    void of_finalAmount_null(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(3000L);
        Money lineTotal = Money.wons(27000L);
        Money itemCouponDiscount = Money.wons(1000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("항목 최종 결제 금액은 필수 입니다.");
    }

    @Test
    @DisplayName("상품 원 가격 총액이 상품 할인 가격 총액보다 작으면 예외가 발생한다")
    void of_originalAmount_lessThan_itemDiscount(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(31000L);
        Money lineTotal = Money.wons(27000L);
        Money itemCouponDiscount = Money.wons(1000L);
        Money finalAmount = Money.wons(26000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, finalAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ITEM_DISCOUNT_EXCEEDS_ORIGINAL_AMOUNT);
    }

    @Test
    @DisplayName("유효하지 않은 상품 판매가 총액인 경우 예외가 발생한다.")
    void of_invalid_lineTotal(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(3000L);
        Money lineTotal = Money.wons(25000L);
        Money itemCouponDiscount = Money.wons(1000L);
        Money finalAmount = Money.wons(26000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, finalAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_ORDER_ITEM_LINE_TOTAL);
    }

    @Test
    @DisplayName("상품 판매가 총액이 상품 쿠폰 할인 금액보다 작으면 예외가 발생한다.")
    void of_lineTotal_lessThan_itemCouponDiscount(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(3000L);
        Money lineTotal = Money.wons(27000L);
        Money itemCouponDiscount = Money.wons(30000L);
        Money finalAmount = Money.wons(26000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, finalAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.ITEM_COUPON_DISCOUNT_EXCEEDS_LINE_TOTAL);
    }

    @Test
    @DisplayName("주문 항목 최종 금액이 맞지 않으면 예외가 발생한다.")
    void of_invalid_finalAmount(){
        //given
        Money originalAmount = Money.wons(30000L);
        Money itemDiscount = Money.wons(3000L);
        Money lineTotal = Money.wons(27000L);
        Money itemCouponDiscount = Money.wons(1000L);
        Money finalAmount = Money.wons(28000L);
        //when
        //then
        assertThatThrownBy(() -> OrderItemAmount.of(originalAmount, itemDiscount, lineTotal, itemCouponDiscount, finalAmount))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_ORDER_ITEM_FINAL_AMOUNT);
    }
}