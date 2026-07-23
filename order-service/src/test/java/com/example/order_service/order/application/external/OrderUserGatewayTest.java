package com.example.order_service.order.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.gateway.UserGatewayErrorCode;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.order.application.external.dto.result.OrdererPointResult;
import com.example.order_service.order.application.external.dto.result.OrdererProfileResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private UserAdaptor userAdaptor;

    @Test
    @DisplayName("주문자 정보를 조회한다")
    void getOrdererProfile() {
        //given
        Long userId = 1L;
        UserProfileResponse.ShippingAddressResponse defaultShippingAddress = UserProfileResponse.ShippingAddressResponse.builder()
                .receiverName("수령인")
                .receiverPhone("010-1234-5678")
                .zipCode("12345")
                .address("서울시 테헤란로 123")
                .addressDetail("123동 1234호")
                .build();
        UserProfileResponse profileResponse = createProfileResponse(defaultShippingAddress);
        given(userAdaptor.getUserProfile(anyLong())).willReturn(profileResponse);
        //when
        OrdererProfileResult ordererProfile = orderUserGateway.getOrdererProfile(userId);
        //then
        assertThat(ordererProfile.orderer())
                .extracting(Orderer::getUserId, Orderer::getUserName, Orderer::getPhoneNumber)
                .containsExactly(profileResponse.userId(), profileResponse.userName(), profileResponse.phoneNumber());

        assertThat(ordererProfile.availablePoints()).isEqualTo(Money.wons(1000L));

        assertThat(ordererProfile.defaultShippingAddress())
                .extracting(ShippingAddress::getReceiverName, ShippingAddress::getReceiverPhone, ShippingAddress::getZipCode,
                        ShippingAddress::getAddress, ShippingAddress::getAddressDetail)
                .containsExactly(
                        defaultShippingAddress.receiverName(), defaultShippingAddress.receiverPhone(), defaultShippingAddress.zipCode(),
                        defaultShippingAddress.address(), defaultShippingAddress.addressDetail()
                );
    }

    @Test
    @DisplayName("주문자 정보 조회중 요청 오류가 발생한 경우 게이트웨이 예외로 변환된다.")
    void getOrdererProfile_throw_client_error() {
        //given
        Long userId = 1L;
        given(userAdaptor.getUserProfile(anyLong())).willThrow(new ExternalClientException("NOT_FOUND_USER", "유저를 찾을 수 없습니다."));
        //when
        //then
        assertThatThrownBy(() -> orderUserGateway.getOrdererProfile(userId))
                .isInstanceOf(DefaultGatewayException.class)
                .extracting("errorCode")
                .isEqualTo(UserGatewayErrorCode.USER_CLIENT_ERROR);
    }

    @Test
    @DisplayName("주문자 정보 조회중 서버 오류가 발생한 경우 게이트웨이 예외로 변환된다.")
    void getOrdererProfile_throw_server_error() {
        //given
        Long userId = 1L;
        given(userAdaptor.getUserProfile(anyLong())).willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "알 수 없는 에러가 발생했습니다."));
        //when
        //then
        assertThatThrownBy(() -> orderUserGateway.getOrdererProfile(userId))
                .isInstanceOf(DefaultGatewayException.class)
                .extracting("errorCode")
                .isEqualTo(UserGatewayErrorCode.USER_SERVER_ERROR);
    }

    @Test
    @DisplayName("주문자 정보 조회중 서버 가용 불가 오류가 발생한 경우 게이트웨이 예외로 변환된다.")
    void getOrdererProfile_throw_unavailable_server_error() {
        //given
        Long userId = 1L;
        given(userAdaptor.getUserProfile(anyLong())).willThrow(new ExternalSystemUnavailableException("INTERNAL_SERVER_ERROR", "알 수 없는 에러가 발생했습니다."));
        //when
        //then
        assertThatThrownBy(() -> orderUserGateway.getOrdererProfile(userId))
                .isInstanceOf(DefaultGatewayException.class)
                .extracting("errorCode")
                .isEqualTo(UserGatewayErrorCode.USER_UNAVAILABLE_SERVER_ERROR);
    }

    @Test
    @DisplayName("주문자 정보 조회중 서킷 오류가 발생한 경우 게이트웨이 예외로 변환된다.")
    void getOrdererProfile_throw_circuit_error() {
        //given
        Long userId = 1L;
        given(userAdaptor.getUserProfile(anyLong())).willThrow(new ExternalCircuitBreakerException("USER_SERVICE_CIRCUIT_OPEN", "통신이 불안정하여 서킷 브레이커가 열렸습니다."));
        //when
        //then
        assertThatThrownBy(() -> orderUserGateway.getOrdererProfile(userId))
                .isInstanceOf(DefaultGatewayException.class)
                .extracting("errorCode")
                .isEqualTo(UserGatewayErrorCode.USER_CIRCUIT_OPEN);
    }

    private UserProfileResponse createProfileResponse(UserProfileResponse.ShippingAddressResponse defaultShippingAddress) {
        return UserProfileResponse.builder()
                .userId(1L)
                .userName("주문자")
                .phoneNumber("010-1234-5678")
                .availablePoints(1000L)
                .defaultShippingAddress(defaultShippingAddress)
                .build();
    }

    @Test
    @DisplayName("사용자의 포인트 잔액을 조회한다")
    void getOrdererPoints(){
        //given
        Long userId = 1L;
        UserPointsResponse response = Instancio.create(UserPointsResponse.class);
        given(userAdaptor.getUserPoints(anyLong())).willReturn(response);
        //when
        OrdererPointResult result = orderUserGateway.getOrdererPoints(userId);
        //then
        assertThat(result.userId()).isEqualTo(response.userId());
        assertThat(result.availablePoints()).isEqualTo(Money.wons(response.availablePoints()));
    }


    @Test
    @DisplayName("유저 포인트 잔액 조회중 외부 시스템 예외가 발생한 경우 가용 포인트는 0원으로 매핑되어 반환된다")
    void getOrdererPoints_ExternalSystemException(){
        //given
        String code = "NOT_FOUND_USER";
        String message = "유저를 찾을 수 없습니다";
        Long userId = 1L;
        willThrow(new ExternalClientException(code, message))
                .given(userAdaptor).getUserPoints(anyLong());
        //when
        OrdererPointResult result = orderUserGateway.getOrdererPoints(userId);
        //then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.availablePoints()).isEqualTo(Money.ZERO);
    }
}
