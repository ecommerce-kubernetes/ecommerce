package com.example.order_service.api.order.facade;

import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.support.fixture.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.api.support.fixture.OrderCouponFixture.anOrderCouponInfo;
import static com.example.order_service.api.support.fixture.OrderFixture.*;
import static com.example.order_service.api.support.fixture.OrderPriceFixture.anCalculatedOrderAmounts;
import static com.example.order_service.api.support.fixture.OrderUserFixture.anOrderUserInfo;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class OrderCreationContextMapperTest {

    @InjectMocks
    private OrderCreationContextMapper mapper;

    @Test
    @DisplayName("주문 생성 Context를 매핑한다")
    void mapOrderCreationContext(){
        //given
        OrderUserInfo user = anOrderUserInfo().build();
        CalculatedOrderAmounts amounts = anCalculatedOrderAmounts().build();
        OrderCouponInfo coupon = anOrderCouponInfo().build();
        CreateOrderCommand commands = OrderCommandFixture.anOrderCommand().build();
        List<OrderProductInfo> products = List.of(OrderProductFixture.anOrderProductInfo().build());

        OrderCreationContext expectedResult = anOrderCreationContext().build();
        //when
        OrderCreationContext result = mapper.mapOrderCreationContext(user, amounts, coupon, commands, products);
        //then

        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

}
