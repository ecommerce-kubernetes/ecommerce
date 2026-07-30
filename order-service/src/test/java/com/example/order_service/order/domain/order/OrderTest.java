package com.example.order_service.order.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    @DisplayName("주문을 생성할때 주문자가 누락되면 예외가 발생한다.")
    void create_orderer_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문을 생성할때 배송 정보가 누락되면 예외가 발생한다.")
    void create_shippingAddress_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문을 생성할때 주문 가격 정보가 누락되면 예외가 발생한다.")
    void create_orderAmount_null() {
        //given
        //when
        //then
    }

    @Test
    @DisplayName("주문을 생성할때 주문 항목은 비어있을 수 없다")
    void create_orderItems_empty() {
        //given
        //when
        //then
    }

}