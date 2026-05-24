package com.example.order_service.order.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.order.application.dto.result.OrderUserResult;
import com.example.order_service.order.application.mapper.OrderUserMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUserGateway {
    private final UserAdaptor userAdaptor;
    private final OrderUserMapper mapper;

    public OrderUserResult.OrdererInfo getUser(Long userId) {
        UserClientResponse.UserInfo userInfo = fetchUserWithTranslation(userId);
        return mapper.toResult(userInfo);
    }

    public OrderUserResult.UserPoint getUserPointsForOrder(Long userId, Money orderAmount, Money usedPoints) {
        UserClientResponse.UserPoints userPoints = fetchUserPointsForOrderWithTranslation(userId, orderAmount, usedPoints);
        return mapper.toResult(userPoints);
    }

    private UserClientResponse.UserInfo fetchUserWithTranslation(Long userId) {
        try {
            return userAdaptor.getUserInfoForOrder(userId);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

    private UserClientResponse.UserPoints fetchUserPointsForOrderWithTranslation(Long userId, Money orderAmount, Money usedPoints) {
        try {
            return userAdaptor.getUserPointsForOrder(userId, orderAmount.longValue(), usedPoints.longValue());
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
