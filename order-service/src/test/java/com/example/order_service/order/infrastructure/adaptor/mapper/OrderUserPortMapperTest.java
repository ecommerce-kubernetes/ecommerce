package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.order.application.port.dto.result.OrdererPointResult;
import com.example.order_service.order.application.port.dto.result.OrdererProfileResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderUserPortMapperTest {

    private final OrderUserPortMapper orderUserPortMapper = new OrderUserPortMapper();

    @Test
    @DisplayName("유저 프로필 조회 결과를 매핑한다")
    void mapToOrdererProfileResult(){
        //given
        UserProfileResponse.ShippingAddressResponse defaultShippingAddress = UserProfileResponse.ShippingAddressResponse.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();

        UserProfileResponse response = UserProfileResponse.builder()
                .userId(1L)
                .userName("주문자")
                .phoneNumber("010-1234-5678")
                .availablePoints(1000L)
                .defaultShippingAddress(defaultShippingAddress)
                .build();
        //when
        OrdererProfileResult result = orderUserPortMapper.mapToOrdererProfileResult(response);
        //then
        assertThat(result.orderer())
                .extracting("userId", "userName", "phoneNumber")
                .containsExactly(1L, "주문자", "010-1234-5678");

        assertThat(result.availablePoints()).isEqualTo(Money.wons(1000L));

        assertThat(result.defaultShippingAddress())
                .extracting("receiverName", "zipCode", "address")
                .containsExactly(defaultShippingAddress.receiverName(), defaultShippingAddress.zipCode(), defaultShippingAddress.address());
    }

    @Test
    @DisplayName("유저 프로필 조회 결과 배송 정보가 없으면 null로 매핑한다")
    void mapToOrdererProfileResult_defaultShippingAddress_null(){
        //given
        UserProfileResponse response = UserProfileResponse.builder()
                .userId(1L)
                .userName("주문자")
                .phoneNumber("010-1234-5678")
                .availablePoints(1000L)
                .defaultShippingAddress(null)
                .build();
        //when
        OrdererProfileResult result = orderUserPortMapper.mapToOrdererProfileResult(response);
        //then
        assertThat(result.defaultShippingAddress()).isNull();
    }

    @Test
    @DisplayName("유저 포인트 조회 결과를 매핑한다")
    void mapToOrdererPointResult(){
        //given
        UserPointsResponse response = UserPointsResponse.builder()
                .userId(1L)
                .availablePoints(1000L)
                .build();
        //when
        OrdererPointResult result = orderUserPortMapper.mapToOrdererPointResult(response);
        //then
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.availablePoints()).isEqualTo(Money.wons(1000L));
    }
}