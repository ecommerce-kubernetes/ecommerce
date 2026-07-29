package com.example.order_service.order.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    @DisplayName("주문 항목을 생성할때 상품 정보가 누락되면 예외가 발생한다.")
    void create_productSnapshot_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문 항목을 생성할때 상품 가격 정보가 누락되면 예외가 발생한다.")
    void create_productPriceSnapshot_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문 항목을 생성할때 수량은 1개 이상이여야 한다.")
    void create_quantity_lessThan_1() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문 항목을 생성할때 상품 옵션이 누락되면 예외가 발생한다.")
    void create_options_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문 항목을 생성할때 주문 가격 정보가 누락되면 예외가 발생한다.")
    void create_orderItemAmount_null() {
        //given
        //when
        //then
    }

}