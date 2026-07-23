package com.example.order_service.order.domain.vo;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrdererTest {

    @Test
    @DisplayName("주문자를 생성한다")
    void of() {
        //given
        //when
        Orderer orderer = Orderer.of(1L, "주문자", "010-1234-5678");
        //then
        assertThat(orderer)
                .extracting(Orderer::getUserId, Orderer::getUserName, Orderer::getPhoneNumber)
                .containsExactlyInAnyOrder(
                        1L, "주문자", "010-1234-5678"
                );
    }

    @Test
    @DisplayName("유저 아이디가 없으면 예외가 발생한다")
    void of_userId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Orderer.of(null, "주문자", "010-1234-6789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저 아이디는 필수 입니다.");
    }

    @Test
    @DisplayName("유저 이름이 없으면 예외가 발생한다.")
    void of_userName_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Orderer.of(1L, "", "010-1234-5678"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저 이름은 필수 입니다.");
    }

    @Test
    @DisplayName("전화 번호가 유효하지 않으면 예외가 발생한다.")
    void of_invalid_phoneNumber() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Orderer.of(1L, "주문자", "1233"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_PHONE_NUMBER);
    }
}