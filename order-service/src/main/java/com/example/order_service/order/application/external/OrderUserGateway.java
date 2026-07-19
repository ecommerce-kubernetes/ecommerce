package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.UserGatewayErrorCode;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.order.application.external.dto.result.OrderUserResult;
import com.example.order_service.order.application.external.dto.result.OrdererPointResult;
import com.example.order_service.order.application.external.dto.result.OrdererProfileResult;
import com.example.order_service.order.application.external.mapper.OrderUserMapper;
import com.example.order_service.order.exception.OrderErrorCode;
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
    private final OrderUserMapper mapper;

    public OrdererProfileResult getOrdererProfile(Long userId) {
        UserProfileResponse response = executeGetUserProfile(userId);
        return mapper.toOrdererProfileResult(response);
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
        return null;
    }

    /**
     * 유저 도메인에 유저 프로필 정보(유저 기본정보, 유저 배송 정보)를 조회
     *
     * @param userId 조회 대상 유저 아이디
     * @return 유저 기본정보, 배송 정보 결과 반환
     */
    @Deprecated
    public OrderUserResult.Profile getUserProfile(Long userId) {
        UserClientResponse.Profile profile = fetchUserProfileWithTranslation(userId);
        return mapper.toResult(profile);
    }

    private UserClientResponse.Profile fetchUserProfileWithTranslation(Long userId) {
        return null;
    }

    /**
     * 유저 도메인에 유저 포인트 정보(포인트 잔액, 적용 가능 포인트)를 조회
     *
     * @param userId      조회 대상 유저 아이디
     * @return 포인트 잔액, 적용 가능 포인트
     */
    public OrderUserResult.UserPoint getUserPoints(Long userId) {
        UserClientResponse.UserPoints userPoints = fetchUserPointsWithTranslation(userId);
        return mapper.toResult(userPoints);
    }

    private UserClientResponse.UserPoints fetchUserPointsWithTranslation(Long userId) {
        try {
            return userAdaptor.getUserPoints(userId);
        } catch (ExternalClientException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_USER_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_USER_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_USER_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_USER_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }
}
