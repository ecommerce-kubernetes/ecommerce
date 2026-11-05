package com.example.order_service.order.domain.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCancelInfoTest {

    @Test
    @DisplayName("주문 취소 이유가 누락되면 예외가 발생한다.")
    void of_reason_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> OrderCancelInfo.of("", LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 취소 이유는 필수이다.");
    }

    @Test
    @DisplayName("주문 취소 시간이 누락되면 예외가 발생한다.")
    void of_canceledAt_null(){
        //given
        //when
        //then
        assertThatThrownBy(() -> OrderCancelInfo.of("단순 변심", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 취소 시간은 필수이다.");
    }

}