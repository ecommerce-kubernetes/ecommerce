package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.common.mapper.MoneyMapperImpl;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@Slf4j
public class OrderUserMapperTest {

    private final MoneyMapper moneyMapper = new MoneyMapperImpl();
    private final OrderUserMapper mapper = new OrderUserMapperImpl(moneyMapper);

    @Test
    @DisplayName("유저 프로필 응답을 Result로 매핑한다")
    void toResult_profile() {
        //given
        UserClientResponse.ShippingInfo shippingResponse = Instancio.of(UserClientResponse.ShippingInfo.class)
                .set(field("receiverPhone"), "010-1234-5678")
                .create();
        UserClientResponse.Profile response = Instancio.of(UserClientResponse.Profile.class)
                .set(field("phoneNumber"), "010-1234-5678")
                .set(field("defaultShippingInfo"), shippingResponse)
                .create();
        Orderer orderer = Orderer.of(response.userId(), response.userName(), response.phoneNumber());
        ShippingAddress shippingAddress = ShippingAddress.of(
                response.defaultShippingInfo().receiverName(),
                response.defaultShippingInfo().receiverPhone(),
                response.defaultShippingInfo().zipCode(),
                response.defaultShippingInfo().address(),
                response.defaultShippingInfo().addressDetail()
        );
        OrderUserResult.Profile expected = OrderUserResult.Profile.builder()
                .orderer(orderer)
                .shippingAddress(shippingAddress)
                .build();
        //when
        OrderUserResult.Profile result = mapper.toResult(response);
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
