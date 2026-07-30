package com.example.order_service.order.domain.vo;

import com.example.order_service.common.exception.BusinessException;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingAddressTest {

    @Test
    @DisplayName("배송 정보를 생성한다")
    void of() {
        //given
        //when
        ShippingAddress shippingAddress = ShippingAddress.of("수령인", "010-1234-5678",
                "12345", "서울시 테헤란로 123", "123동 1235호");
        //then
        assertThat(shippingAddress)
                .extracting(ShippingAddress::getReceiverName, ShippingAddress::getReceiverPhone, ShippingAddress::getZipCode, ShippingAddress::getAddress, ShippingAddress::getAddressDetail)
                .containsExactlyInAnyOrder(
                        "수령인", "010-1234-5678", "12345", "서울시 테헤란로 123", "123동 1235호"
                );
    }

    @Test
    @DisplayName("수령인 이름이 없으면 예외가 발생한다")
    void of_receiverName_null() {
        //given
        String receiverName = "";
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of(receiverName, "010-1234-5678", "12345",
                "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수령인 이름은 필수 입니다.");
    }

    @Test
    @DisplayName("수령인 전화번호가 누락되면 예외가 발생한다.")
    void of_receiverPhone_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", null, "12345",
                "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수령인 전화번호는 필수 입니다.");
    }

    @Test
    @DisplayName("수령인 전화번호가 유효하지 않으면 예외가 발생한다.")
    void of_invalid_receiverPhone() {
        //given
        String receiverPhone = "123";
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", receiverPhone, "12345",
                "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_PHONE_NUMBER);
    }

    @Test
    @DisplayName("우편번호가 누락되면 예외가 발생한다.")
    void of_zipCode_null() {
        //given
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", "010-1234-5678", null,
                "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("우편 번호는 필수 입니다.");
    }

    @Test
    @DisplayName("우편 번호가 유효하지 않으면 예외가 발생한다.")
    void of_invalid_zipCode() {
        //given
        String zipCode = "asdf";
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", "010-1234-5678", zipCode,
                "서울시 테헤란로 123", "123동 1234호"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(OrderErrorCode.INVALID_ZIPCODE);
    }

    @Test
    @DisplayName("주소가 없으면 예외가 발생한다.")
    void of_invalid_address() {
        //given
        String address = "";
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", "010-1234-5678", "12345", address,
                "123동 1234호"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주소는 필수 입니다.");
    }

    @Test
    @DisplayName("상세 주소가 없으면 예외가 발생한다.")
    void of_invalid_addressDetail() {
        //given
        String addressDetail = "";
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.of("수령인", "010-1234-5678", "12345",
                "서울시 테헤란로 123", addressDetail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상세 주소는 필수 입니다.");
    }
}