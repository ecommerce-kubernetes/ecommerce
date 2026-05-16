package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderSheetUserMapperTest {

    private final OrderSheetUserMapper mapper = new OrderSheetUserMapperImpl();

    @Test
    @DisplayName("유저 응답을 Result로 매핑한다")
    void toResult() {
        //given
        UserClientResponse.Profile response = fixtureMonkey.giveMeOne(UserClientResponse.Profile.class);
        OrderSheetUserResult.ShippingAddress shippingAddress = OrderSheetUserResult.ShippingAddress.builder()
                .receiverName(response.defaultShippingAddress().receiverName())
                .receiverPhone(response.defaultShippingAddress().receiverPhone())
                .zipCode(response.defaultShippingAddress().zipCode())
                .address(response.defaultShippingAddress().address())
                .addressDetail(response.defaultShippingAddress().addressDetail())
                .build();
        OrderSheetUserResult.Profile expectedResult = OrderSheetUserResult.Profile.builder()
                .userId(response.userId())
                .userName(response.userName())
                .phoneNumber(response.phoneNumber())
                .shippingAddress(shippingAddress)
                .build();

        //when
        OrderSheetUserResult.Profile result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }
}
