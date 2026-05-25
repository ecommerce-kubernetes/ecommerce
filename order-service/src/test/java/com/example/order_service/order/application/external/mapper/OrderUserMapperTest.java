package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.common.mapper.MoneyMapperImpl;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.external.mapper.OrderUserMapper;
import com.example.order_service.order.application.external.mapper.OrderUserMapperImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderUserMapperTest {

    private final MoneyMapper moneyMapper = new MoneyMapperImpl();
    private final OrderUserMapper mapper = new OrderUserMapperImpl(moneyMapper);

    @Test
    @DisplayName("유저 응답을 Result로 매핑한다")
    void toResult() {
        //given
        UserClientResponse.Profile response = fixtureMonkey.giveMeOne(UserClientResponse.Profile.class);
        OrderUserResult.ShippingAddress shippingAddress = OrderUserResult.ShippingAddress.builder()
                .receiverName(response.defaultShippingAddress().receiverName())
                .receiverPhone(response.defaultShippingAddress().receiverPhone())
                .zipCode(response.defaultShippingAddress().zipCode())
                .address(response.defaultShippingAddress().address())
                .addressDetail(response.defaultShippingAddress().addressDetail())
                .build();
        OrderUserResult.Profile expectedResult = OrderUserResult.Profile.builder()
                .userId(response.userId())
                .userName(response.userName())
                .phoneNumber(response.phoneNumber())
                .shippingAddress(shippingAddress)
                .build();

        //when
        OrderUserResult.Profile result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }
}
