package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetUserMapper;
import com.example.order_service.ordersheet.application.mapper.OrderSheetUserMapperTest;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
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
public class OrderSheetUserGatewayTest {

    @InjectMocks
    private OrderSheetUserGateway orderSheetUserGateway;
    @Mock
    private UserAdaptor adaptor;
    @Mock
    private OrderSheetUserMapper userMapper;

    @Nested
    @DisplayName("유저 정보 조회")
    class GetProfile {

        @Test
        @DisplayName("유저 프로필 정보를 조회한다")
        void getUserProfile() {
            //given
            Long userId = 1L;
            UserClientResponse.Profile userResponse = fixtureMonkey.giveMeOne(UserClientResponse.Profile.class);
            OrderSheetUserResult.Profile profile = fixtureMonkey.giveMeOne(OrderSheetUserResult.Profile.class);
            given(adaptor.getUserProfile(anyLong())).willReturn(userResponse);
            given(userMapper.toResult(any())).willReturn(profile);
            //when
            OrderSheetUserResult.Profile userProfile = orderSheetUserGateway.getUserProfile(userId);
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
            assertThatThrownBy(() -> orderSheetUserGateway.getUserProfile(userId))
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
            assertThatThrownBy(() -> orderSheetUserGateway.getUserProfile(userId))
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
            assertThatThrownBy(() -> orderSheetUserGateway.getUserProfile(userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
