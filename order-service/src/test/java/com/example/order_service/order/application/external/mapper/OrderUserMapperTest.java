package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.common.mapper.MoneyMapperImpl;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
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
        ShippingAddress shippingAddress = ShippingAddress.of(response.defaultShippingAddress().receiverName(),
                        response.defaultShippingAddress().receiverPhone(),
                        response.defaultShippingAddress().zipCode(),
                        response.defaultShippingAddress().address(),
                        response.defaultShippingAddress().addressDetail());
        Orderer orderer = Orderer.of(response.userId(), response.userName(), response.phoneNumber());
        OrderUserResult.Profile expectedResult = OrderUserResult.Profile.builder()
                .orderer(orderer)
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
