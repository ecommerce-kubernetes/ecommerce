package com.example.order_service.order.domain.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingAddressTest {

    @Test
    @DisplayName("배송 정보를 생성한다")
    void of() {
        //given
        //when
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1235호");
        //then
        assertThat(shippingAddress)
                .extracting("receiverName", "receiverPhone", "zipCode", "address", "addressDetail")
                .containsExactlyInAnyOrder(
                        "수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1235호"
                );
    }

    @ParameterizedTest(name = "이름이 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_receiverName(String receiverName) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of(receiverName, "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수령인 이름은 필수 입니다");
    }

    @ParameterizedTest(name = "전화번호가 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, '', 1231215"},
            nullValues = "null"
    )
    void of_invalid_receiverPhone(String phone) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", phone, "12345", "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 전화번호 형식입니다.");
    }

    @ParameterizedTest(name = "전화번호가 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_zipCode(String zipCode) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", "010-1234-5678", zipCode, "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("우편번호는 필수입니다");
    }

    @ParameterizedTest(name = "전화번호가 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_address(String address) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", "010-1234-5678", "12345", address, "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주소는 필수입니다");
    }

    @ParameterizedTest(name = "전화번호가 유효하지 않으면 예외가 발생한다")
    @CsvSource(
            value = {"null, ''"},
            nullValues = "null"
    )
    void of_invalid_addressDetail(String addressDetail) {
        //given
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", addressDetail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상세 주소는 필수입니다");
    }
}