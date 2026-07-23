package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.common.mapper.MoneyMapperImpl;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.external.dto.result.OrdererProfileResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class OrderUserMapperTest {

    private final MoneyMapper moneyMapper = new MoneyMapperImpl();
    private final OrderUserMapper mapper = new OrderUserMapperImpl(moneyMapper);

    @Test
    @DisplayName("유저 프로필 응답을 매핑한다")
    void toOrdererProfileResult() {
        //given
        UserProfileResponse.ShippingAddressResponse shippingAddressResponse = UserProfileResponse.ShippingAddressResponse.builder()
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
                .defaultShippingAddress(shippingAddressResponse)
                .build();

        Orderer orderer = Orderer.of(response.userId(), response.userName(), response.phoneNumber());
        ShippingAddress shippingAddress = ShippingAddress.of(shippingAddressResponse.receiverName(), shippingAddressResponse.receiverPhone(), shippingAddressResponse.zipCode(),
                shippingAddressResponse.address(), shippingAddressResponse.addressDetail());

        OrdererProfileResult expected = OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(Money.wons(1000L))
                .defaultShippingAddress(shippingAddress)
                .build();

        //when
        OrdererProfileResult result = mapper.toOrdererProfileResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("유저 프로필 응답을 매핑할때 사용가능 포인트가 없으면 0원으로 매핑된다")
    void toOrdererProfileResult_availablePoints_null(){
        //given
        UserProfileResponse.ShippingAddressResponse shippingAddressResponse = UserProfileResponse.ShippingAddressResponse.builder()
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
                .availablePoints(null)
                .defaultShippingAddress(shippingAddressResponse)
                .build();

        Orderer orderer = Orderer.of(response.userId(), response.userName(), response.phoneNumber());
        ShippingAddress shippingAddress = ShippingAddress.of(shippingAddressResponse.receiverName(), shippingAddressResponse.receiverPhone(), shippingAddressResponse.zipCode(),
                shippingAddressResponse.address(), shippingAddressResponse.addressDetail());

        OrdererProfileResult expected = OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(Money.ZERO)
                .defaultShippingAddress(shippingAddress)
                .build();
        //when
        OrdererProfileResult result = mapper.toOrdererProfileResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("유저 포인트 응답을 Result로 매핑한다")
    void toResult_point(){
        //given
        UserClientResponse.UserPoints response = Instancio.create(UserClientResponse.UserPoints.class);
        OrderUserResult.UserPoint expected = OrderUserResult.UserPoint.builder()
                .userId(response.userId())
                .ownedPoints(Money.wons(response.ownedPoints()))
                .build();
        //when
        OrderUserResult.UserPoint result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
