package com.example.userservice.user.domain;

import com.example.userservice.common.util.IdGenerator;
import com.example.userservice.common.util.TsidGenerator;
import com.example.userservice.user.domain.context.CreateShippingAddressContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingAddressTest {

    private final IdGenerator idGenerator = new TsidGenerator();

    @Test
    @DisplayName("배송지를 생성한다.")
    void create() {
        //given
        CreateShippingAddressContext context = aContext(true);
        //when
        ShippingAddress shippingAddress = ShippingAddress.create(context, idGenerator, true);
        //then
        assertThat(shippingAddress.getId()).isNotNull();
        assertThat(shippingAddress.getReceiverName()).isEqualTo("수령인");
        assertThat(shippingAddress.getReceiverPhone()).isEqualTo("010-1234-5678");
        assertThat(shippingAddress.getZipCode()).isEqualTo("12345");
        assertThat(shippingAddress.getAddress()).isEqualTo("서울시 테헤란로 123");
        assertThat(shippingAddress.getAddressDetail()).isEqualTo("123동 1234호");
        assertThat(shippingAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("수령인 이름이 없으면 예외가 발생한다.")
    void create_whenReceiverNameBlank_thenThrownException() {
        //given
        CreateShippingAddressContext context = CreateShippingAddressContext.builder()
                .receiverName("")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .isDefault(false)
                .build();
        //when
        //then
        assertThatThrownBy(() -> ShippingAddress.create(context, idGenerator, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("대표 배송지로 승격한다.")
    void promoteToDefault() {
        //given
        ShippingAddress shippingAddress = ShippingAddress.create(aContext(false), idGenerator, false);
        //when
        shippingAddress.promoteToDefault();
        //then
        assertThat(shippingAddress.isDefault()).isTrue();
    }

    @Test
    @DisplayName("대표 배송지에서 해제한다.")
    void demoteFromDefault() {
        //given
        ShippingAddress shippingAddress = ShippingAddress.create(aContext(true), idGenerator, true);
        //when
        shippingAddress.demoteFromDefault();
        //then
        assertThat(shippingAddress.isDefault()).isFalse();
    }

    private CreateShippingAddressContext aContext(boolean isDefault) {
        return CreateShippingAddressContext.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .isDefault(isDefault)
                .build();
    }
}
