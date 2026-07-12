package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.external.mapper.OrderUserMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderUserGatewayTest {

    @InjectMocks
    private OrderUserGateway orderUserGateway;
    @Mock
    private UserAdaptor adaptor;
    @Mock
    private OrderUserMapper userMapper;

    @Nested
    @DisplayName("유저 정보 조회")
    class GetProfile {

        @Test
        @DisplayName("유저 프로필 정보를 조회한다")
        void getUserProfile() {
            //given
            Long userId = 1L;
            UserClientResponse.Profile userResponse = Instancio.create(UserClientResponse.Profile.class);
            OrderUserResult.Profile profile = Instancio.create(OrderUserResult.Profile.class);
            given(adaptor.getUserProfile(anyLong())).willReturn(userResponse);
            given(userMapper.toResult(any(UserClientResponse.Profile.class))).willReturn(profile);
            //when
            OrderUserResult.Profile userProfile = orderUserGateway.getUserProfile(userId);
            //then
            assertThat(userProfile).isEqualTo(profile);
        }
        
        @Test
        @DisplayName("유저 조회중 유저 서비스에서 클라이언트 오류가 발생한 경우 예외가 발생한다")
        void getUserProfile_ExternalClientException() {
            //given
            String code = "NOT_FOUND_USER";
            String message = "유저를 찾을 수 없습니다";
            Long userId = 1L;
            willThrow(new ExternalClientException(code, message))
                    .given(adaptor).getUserProfile(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserProfile(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_CLIENT_ERROR, code);
        }

        @Test
        @DisplayName("유저 조회중 유저 서비스에서 서버 오류가 발생한 경우 예외가 발생한다")
        void getUserProfile_ExternalServerException() {
            //given
            String code = "INTERNAL_SERVER_ERROR";
            String message = "알 수 없는 오류가 발생했습니다";
            Long userId = 1L;
            willThrow(new ExternalServerException(code, message))
                    .given(adaptor).getUserProfile(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserProfile(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("유저 조회중 유저 서비스에서 서버 오류가 발생한 경우 예외가 발생한다")
        void getUserProfile_ExternalUnavailableServerException() {
            //given
            String code = "SERVICE_UNAVAILABLE";
            String message = "유저 서비스 통신 장애";
            Long userId = 1L;
            willThrow(new ExternalSystemUnavailableException(code, message))
                    .given(adaptor).getUserProfile(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserProfile(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_UNAVAILABLE_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("유저 조회중 유저 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
        void getUserProfile_ExternalCircuitBreakerException() {
            //given
            String code = "CIRCUIT_OPEN";
            String message = "유저 서킷 열림";
            Long userId = 1L;
            willThrow(new ExternalCircuitBreakerException(code, message))
                    .given(adaptor).getUserProfile(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserProfile(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_CIRCUIT_OPEN, code);
        }
    }

    @Nested
    @DisplayName("유저 포인트 잔액 조회")
    class GetUserPoints {

        @Test
        @DisplayName("사용자의 포인트 잔액을 조회한다")
        void getUserPoints(){
            //given
            Long userId = 1L;
            UserClientResponse.UserPoints response = Instancio.create(UserClientResponse.UserPoints.class);
            OrderUserResult.UserPoint userPoint = Instancio.create(OrderUserResult.UserPoint.class);
            given(adaptor.getUserPoints(anyLong())).willReturn(response);
            given(userMapper.toResult(any(UserClientResponse.UserPoints.class))).willReturn(userPoint);
            //when
            OrderUserResult.UserPoint result = orderUserGateway.getUserPoints(userId);
            //then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("유저 포인트 잔액 조회중 클라이언트 오류가 발생한 경우 비지니스 예외가 발생한다")
        void getUserPoints_ExternalClientException(){
            //given
            String code = "NOT_FOUND_USER";
            String message = "유저를 찾을 수 없습니다";
            Long userId = 1L;
            willThrow(new ExternalClientException(code, message))
                    .given(adaptor).getUserPoints(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPoints(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_CLIENT_ERROR, code);
        }

        @Test
        @DisplayName("유저 포인트 잔액 조회중 서버 오류가 발생한 경우 비지니스 예외로 변환된다")
        void getUserPoints_ExternalServerException() {
            //given
            String code = "INTERNAL_SERVER_ERROR";
            String message = "알 수 없는 오류가 발생했습니다";
            Long userId = 1L;
            willThrow(new ExternalServerException(code, message))
                    .given(adaptor).getUserPoints(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPoints(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("유저 포인트 잔액 조회중 서버 오류가 발생한 경우 비지니스 예외로 변환된다")
        void getUserPoints_ExternalUnavailableServerException() {
            //given
            String code = "SERVICE_UNAVAILABLE";
            String message = "유저 서비스 통신 장애";
            Long userId = 1L;
            willThrow(new ExternalSystemUnavailableException(code, message))
                    .given(adaptor).getUserPoints(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPoints(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_UNAVAILABLE_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("유저 포인트 잔액 조회중 유저 서킷이 열린 경우 예외가 발생한다")
        void getUserPoints_ExternalCircuitException() {
            //given
            String code = "CIRCUIT_OPEN";
            String message = "유저 서비스 서킷 열림";
            Long userId = 1L;
            willThrow(new ExternalCircuitBreakerException(code, message))
                    .given(adaptor).getUserPoints(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPoints(userId))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_USER_CIRCUIT_OPEN, code);
        }
    }
}
