package com.example.order_service.order.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.*;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.gateway.UserGatewayErrorCode;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.external.dto.result.OrdererPointResult;
import com.example.order_service.order.application.external.dto.result.OrdererProfileResult;
import com.example.order_service.order.domain.vo.Orderer;
import com.example.order_service.order.domain.vo.ShippingAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 주문 유저 도메인 통신을 담당하는 Gateway 서비스
 * <p>
 * 유저 도메인의 응답을 서비스 레이어의 Result로 매핑하여 반환
 * 유저 도메인 통신중 발생하는 예외를 비지니스 예외로 변환
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 22
 */
@Service
@RequiredArgsConstructor
public class OrderUserGateway {
    private final UserAdaptor userAdaptor;

    public OrdererProfileResult getOrdererProfile(Long userId) {
        UserProfileResponse response = executeGetUserProfile(userId);
        return mapToOrdererProfileResult(response);
    }

    private OrdererProfileResult mapToOrdererProfileResult(UserProfileResponse response) {
        Orderer orderer = Orderer.of(response.userId(), response.userName(), response.phoneNumber());
        ShippingAddress shippingAddress = mapToShippingAddress(response.defaultShippingAddress());
        Money availablePoints = mapToAvailablePoints(response.availablePoints());
        return OrdererProfileResult.builder()
                .orderer(orderer)
                .availablePoints(availablePoints)
                .defaultShippingAddress(shippingAddress)
                .build();
    }

    private ShippingAddress mapToShippingAddress(UserProfileResponse.ShippingAddressResponse response) {
        if (response == null) {
            return null;
        }
        return ShippingAddress.of(response.receiverName(), response.receiverPhone(), response.zipCode(), response.address(),
                response.addressDetail());
    }

    private Money mapToAvailablePoints(Long availablePoints) {
        if (availablePoints == null) {
            return Money.ZERO;
        }
        return Money.wons(availablePoints);
    }

    private UserProfileResponse executeGetUserProfile(Long userId) {
        try {
            return userAdaptor.getUserProfile(userId);
        } catch (ExternalClientException e) {
            throw new DefaultGatewayException(UserGatewayErrorCode.USER_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultGatewayException(UserGatewayErrorCode.USER_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultGatewayException(UserGatewayErrorCode.USER_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultGatewayException(UserGatewayErrorCode.USER_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }

    public OrdererPointResult getOrdererPoints(Long userId) {
        try {
            UserPointsResponse response = executeGetUserPoints(userId);
            return mapToOrdererPointsResult(response);
        } catch (ExternalSystemException e) {
            return OrdererPointResult.builder()
                    .userId(userId)
                    .availablePoints(Money.ZERO)
                    .build();
        }
    }

    private UserPointsResponse executeGetUserPoints(Long userId) {
        return userAdaptor.getUserPoints(userId);
    }

    private OrdererPointResult mapToOrdererPointsResult(UserPointsResponse response) {
        return OrdererPointResult.builder()
                .userId(response.userId())
                .availablePoints(Money.wons(response.availablePoints()))
                .build();
    }

    @Deprecated
    public OrderUserResult.UserPoint getUserPoints(Long userId) {
        return null;
    }
}
