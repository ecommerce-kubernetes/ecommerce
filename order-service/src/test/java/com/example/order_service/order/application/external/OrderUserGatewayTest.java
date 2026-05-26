package com.example.order_service.order.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.external.mapper.OrderUserMapper;
import com.example.order_service.order.exception.OrderSheetErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
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
            UserClientResponse.Profile userResponse = fixtureMonkey.giveMeOne(UserClientResponse.Profile.class);
            OrderUserResult.Profile profile = fixtureMonkey.giveMeOne(OrderUserResult.Profile.class);
            given(adaptor.getUserProfile(anyLong())).willReturn(userResponse);
            given(userMapper.toResult(any(UserClientResponse.Profile.class))).willReturn(profile);
            //when
            OrderUserResult.Profile userProfile = orderUserGateway.getUserProfile(userId);
            //then
            assertThat(userProfile).isNotNull();
        }
        
        @Test
        @DisplayName("유저 조회중 유저 서비스에서 클라이언트 오류가 발생한 경우 비지니스 예외로 변환된다")
        void getUserProfile_ExternalClientException() {
            //given
            Long userId = 1L;
            willThrow(new ExternalClientException("NOT_FOUND_USER", "유저를 찾을 수 없습니다"))
                    .given(adaptor).getUserProfile(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserProfile(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        }

        @Test
        @DisplayName("유저 조회중 유저 서비스에서 서버 오류가 발생한 경우 비지니스 예외로 변환된다")
        void getUserProfile_ExternalServerException() {
            //given
            Long userId = 1L;
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "알 수 없는 오류가 발생했습니다"))
                    .given(adaptor).getUserProfile(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserProfile(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        }

        @Test
        @DisplayName("유저 조회중 유저 서비스에서 서버 오류가 발생한 경우 비지니스 예외로 변환된다")
        void getUserProfile_ExternalUnavailableServerException() {
            //given
            Long userId = 1L;
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "유저 서비스 통신 장애"))
                    .given(adaptor).getUserProfile(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserProfile(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
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
            UserClientResponse.UserPoints response = fixtureMonkey.giveMeOne(UserClientResponse.UserPoints.class);
            OrderUserResult.UserPoint userPoint = fixtureMonkey.giveMeOne(OrderUserResult.UserPoint.class);
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
            Long userId = 1L;
            willThrow(new ExternalClientException("NOT_FOUND_USER", "유저를 찾을 수 없습니다"))
                    .given(adaptor).getUserPoints(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPoints(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        }

        @Test
        @DisplayName("유저 포인트 잔액 조회중 서버 오류가 발생한 경우 비지니스 예외로 변환된다")
        void getUserPoints_ExternalServerException() {
            //given
            Long userId = 1L;
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "알 수 없는 오류가 발생했습니다"))
                    .given(adaptor).getUserPoints(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPoints(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        }

        @Test
        @DisplayName("유저 포인트 잔액 조회중 서버 오류가 발생한 경우 비지니스 예외로 변환된다")
        void getUserPoints_ExternalUnavailableServerException() {
            //given
            Long userId = 1L;
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "유저 서비스 통신 장애"))
                    .given(adaptor).getUserPoints(anyLong());
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPoints(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

    @Nested
    @DisplayName("유저 포인트 잔액 검증")
    class GetUserPointsForOrder {
        @Test
        @DisplayName("유저 포인트 잔액을 검증한다")
        void getUserPointsForOrder() {
            //given
            Long userId = 1L;
            Money usedPoints = Money.wons(1000L);
            UserClientResponse.UserPoints response = fixtureMonkey.giveMeOne(UserClientResponse.UserPoints.class);
            OrderUserResult.UserPoint userPoint = fixtureMonkey.giveMeOne(OrderUserResult.UserPoint.class);
            given(adaptor.getUserPointsForOrder(anyLong(), anyLong())).willReturn(response);
            given(userMapper.toResult(any(UserClientResponse.UserPoints.class))).willReturn(userPoint);
            //when
            OrderUserResult.UserPoint result = orderUserGateway.getUserPointsForOrder(userId, usedPoints);
            //then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("유저 포인트 검증중 클라이언트 에러가 발생한 경우 비지니스 예외가 발생한다")
        void getUserPointsForOrder_ExternalClientException() {
            //given
            Long userId = 1L;
            Money usedPoints = Money.wons(1000L);
            given(adaptor.getUserPointsForOrder(any(), any()))
                    .willThrow(new ExternalClientException("NOT_FOUND_USER", "유저를 찾을 수 없습니다"));
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPointsForOrder(userId, usedPoints))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        }

        @Test
        @DisplayName("유저 포인트 검증중 서버에러가 발생한 경우 비지니스 예외가 발생한다")
        void getUserPointsForOrder_ExternalServerException() {
            //given
            Long userId = 1L;
            Money usedPoints = Money.wons(1000L);
            given(adaptor.getUserPointsForOrder(any(), any()))
                    .willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "알 수 없는 에러가 발생했습니다"));
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPointsForOrder(userId, usedPoints))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        }

        @Test
        @DisplayName("유저 포인트 검증중 서비스 사용 불가 오류가 발생한 경우 비지니스 예외가 발생한다")
        void getUserPointsForOrder_ExternalUnavailableServiceException() {
            //given
            Long userId = 1L;
            Money usedPoints = Money.wons(1000L);
            given(adaptor.getUserPointsForOrder(any(), any()))
                    .willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "유저 서비스 통신 장애"));
            //when
            //then
            assertThatThrownBy(() -> orderUserGateway.getUserPointsForOrder(userId, usedPoints))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

}
