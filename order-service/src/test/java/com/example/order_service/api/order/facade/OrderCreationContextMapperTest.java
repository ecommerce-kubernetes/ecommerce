package com.example.order_service.api.order.facade;

import com.example.order_service.api.order.domain.service.dto.command.OrderCreationContext;
import com.example.order_service.api.order.domain.service.dto.command.PaymentCreationContext;
import com.example.order_service.api.order.domain.service.dto.result.*;
import com.example.order_service.api.order.facade.dto.command.CreateOrderCommand;
import com.example.order_service.api.support.fixture.order.OrderCommandFixture;
import com.example.order_service.api.support.fixture.order.OrderProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.api.support.fixture.order.OrderCouponFixture.anOrderCouponInfo;
import static com.example.order_service.api.support.fixture.order.OrderFixture.anOrderCreationContext;
import static com.example.order_service.api.support.fixture.order.OrderFixture.anPaymentContext;
import static com.example.order_service.api.support.fixture.order.OrderPaymentFixture.anOrderPaymentInfo;
import static com.example.order_service.api.support.fixture.order.OrderPriceFixture.anCalculatedOrderAmounts;
import static com.example.order_service.api.support.fixture.order.OrderUserFixture.anOrderUserInfo;
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

    @Test
    @DisplayName("결제 생성 Context를 매핑한다")
    void mapPaymentCreationContext(){
        //given
        OrderPaymentInfo payment = anOrderPaymentInfo().build();
        PaymentCreationContext expectedResult = anPaymentContext().build();
        //when
        PaymentCreationContext result = mapper.mapPaymentCreationContext(payment);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("approvedAt")
                .isEqualTo(expectedResult);
    }

}
