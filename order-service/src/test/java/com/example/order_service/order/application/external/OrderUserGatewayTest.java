package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import com.example.order_service.order.application.mapper.OrderUserMapper;
import com.example.order_service.order.domain.service.dto.result.OrderUserInfo;
import com.example.order_service.order.infrastructure.client.user.OrderUserAdaptor;
import com.example.order_service.order.infrastructure.client.user.dto.OrderUserResponse;
import com.example.order_service.support.TestFixtureUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.api.support.fixture.order.OrderUserFixture.anOrderUserInfo;
import static com.example.order_service.api.support.fixture.order.OrderUserFixture.anOrderUserResponse;
import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static com.example.order_service.support.TestFixtureUtil.sample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderUserGatewayTest {

    @InjectMocks
    private OrderUserGateway orderUserGateway;

    @Mock
    private OrderUserAdaptor orderUserAdaptor;

    @Mock
    private UserAdaptor adaptor;
    @Spy
    private OrderUserMapper orderUserMapper = Mappers.getMapper(OrderUserMapper.class);

    @Nested
    @DisplayName("유저 정보 조회")
    class GetUser {

        @Test
        @DisplayName("유저 정보를 조회한다")
        void getUser() {
            //given
            Long userId = 1L;
            UserClientResponse.UserInfo response = sample(fixtureMonkey.giveMeBuilder(UserClientResponse.UserInfo.class)
                    .set("userId", userId));
            given(adaptor.getUserInfoForOrder(anyLong()))
                    .willReturn(response);
            //when
            OrderUserResult.OrdererInfo result = orderUserGateway.getUser(userId);
            //then
            assertThat(result)
                    .extracting("userId", "availablePoints", "ordererName", "ordererPhone")
                    .containsExactlyInAnyOrder(
                            userId, response.pointBalance(), response.userName(), response.phoneNumber()
                    );
        }

        @Test
        @DisplayName("유저 서비스에서 서버 예외가 발생한 경우 비지니스 예외로 변환해 던진다")
        void getUser_external_serverException(){
            //given
            Long userId = 1L;
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "처리중 오류가 발생했습니다"))
                    .given(adaptor).getUserInfoForOrder(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUser(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_USER_SERVER_ERROR);
        }

        @Test
        @DisplayName("유저 정보 조회중 클라이언트 오류가 발생한 경우 비지니스 예외로 변경해 던진다")
        void getUser_external_client_exception(){
            //given
            Long userId = 1L;
            willThrow(new ExternalClientException("NOT_FOUND_USER", "유저를 찾을 수 없습니다"))
                    .given(adaptor).getUserInfoForOrder(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUser(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_USER_CLIENT_ERROR);
        }

        @Test
        @DisplayName("유저 정보 조회중 유저 서비스에서 사용 불가 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getUser_external_unavailable_exception(){
            //given
            Long userId = 1L;
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "유저 서비스 통신 장애"))
                    .given(adaptor).getUserInfoForOrder(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUser(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_USER_UNAVAILABLE_SERVER_ERROR);
        }

        @Test
        @DisplayName("유저 정보를 조회한다")
        void getUserdeprecated() {
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
        void getUser_insufficient_point_balance_deprecated() {
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
