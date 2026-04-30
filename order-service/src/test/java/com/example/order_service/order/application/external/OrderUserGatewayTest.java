package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.order.domain.service.dto.result.OrderUserInfo;
import com.example.order_service.order.infrastructure.client.user.OrderUserAdaptor;
import com.example.order_service.order.infrastructure.client.user.dto.OrderUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.api.support.fixture.order.OrderUserFixture.anOrderUserInfo;
import static com.example.order_service.api.support.fixture.order.OrderUserFixture.anOrderUserResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderUserGatewayTest {

    @InjectMocks
    private OrderUserGateway orderUserGateway;

    @Mock
    private OrderUserAdaptor orderUserAdaptor;

    @Nested
    @DisplayName("유저 정보 조회")
    class GetUser {

        @Test
        @DisplayName("유저 정보를 조회한다")
        void getUser(){
            //given
            OrderUserResponse user = anOrderUserResponse().build();
            given(orderUserAdaptor.getUser(anyLong()))
                    .willReturn(user);
            OrderUserInfo expectedResult = anOrderUserInfo().build();
            //when
            OrderUserInfo result = orderUserGateway.getUser(1L, 500L);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("포인트 잔액이 부족하면 예외가 발생한다")
        void getUser_insufficient_point_balance(){
            //given
            OrderUserResponse user = anOrderUserResponse().build();
            given(orderUserAdaptor.getUser(anyLong()))
                    .willReturn(user);
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUser(1L, 1500L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_USER_INSUFFICIENT_POINT_BALANCE);
        }
    }
}
