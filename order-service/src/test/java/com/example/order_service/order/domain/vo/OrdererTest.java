package com.example.order_service.order.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
                .extracting("userId", "userName", "phoneNumber")
                .containsExactlyInAnyOrder(
                        1L, "주문자", "010-1234-5678"
                );
    }

    @Test
    @DisplayName("유저 아이디가 null이면 예외가 발생한다")
    void of_userId_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> Orderer.of(null, "주문자", "010-1234-6789"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저 아이디는 필수값 입니다");
    }

    @ParameterizedTest(name = "이름이 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_userName(String userName) {
        //given
        //when
        //then
        assertThatThrownBy(() -> Orderer.of(1L, userName, "010-2134-5124"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유저 이름은 필수 입니다");
    }

    @ParameterizedTest(name = "전화번호가 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, '', 1231215"},
            nullValues = "null"
    )
    void of_invalid_userPhone(String phone) {
        //given
        //when
        //then
        assertThatThrownBy(() -> Orderer.of(1L, "주문자", phone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 전화번호 형식입니다.");
    }
}