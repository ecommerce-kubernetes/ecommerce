package com.example.order_service.order.infrastructure.adaptor.client;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.*;
import com.example.order_service.common.exception.DefaultPortException;
import com.example.order_service.order.exception.OrderUserPortErrorCode;
import com.example.order_service.infrastructure.dto.response.user.UserPointsResponse;
import com.example.order_service.infrastructure.dto.response.user.UserProfileResponse;
import com.example.order_service.infrastructure.gateway.UserGateway;
import com.example.order_service.order.application.port.OrderUserPort;
import com.example.order_service.order.application.port.dto.OrdererPointResult;
import com.example.order_service.order.application.port.dto.OrdererProfileResult;
import com.example.order_service.order.infrastructure.adaptor.mapper.OrderUserPortMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderUserAdaptor implements OrderUserPort {
    private final UserGateway userGateway;
    private final OrderUserPortMapper orderUserPortMapper;

    @Override
    public OrdererProfileResult getOrdererProfile(Long userId) {
        UserProfileResponse response = executeGetUserProfile(userId);
        return orderUserPortMapper.mapToOrdererProfileResult(response);
    }

    private UserProfileResponse executeGetUserProfile(Long userId) {
        try {
            return userGateway.getUserProfile(userId);
        } catch (ExternalClientException e) {
            throw new DefaultPortException(OrderUserPortErrorCode.USER_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultPortException(OrderUserPortErrorCode.USER_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultPortException(OrderUserPortErrorCode.USER_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultPortException(OrderUserPortErrorCode.USER_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public OrdererPointResult getOrdererPoints(Long userId) {
        try {
            UserPointsResponse response = executeGetUserPoints(userId);
            return orderUserPortMapper.mapToOrdererPointResult(response);
        } catch (ExternalSystemException e) {
            return OrdererPointResult.builder()
                    .userId(userId)
                    .availablePoints(Money.ZERO)
                    .build();
        }
    }

    private UserPointsResponse executeGetUserPoints(Long userId) {
        return userGateway.getUserPoints(userId);
    }
}
