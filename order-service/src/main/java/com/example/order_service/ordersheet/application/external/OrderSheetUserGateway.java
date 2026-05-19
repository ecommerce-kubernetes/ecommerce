package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.UserAdaptor;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetUserResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetUserMapper;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSheetUserGateway {
    private final UserAdaptor userAdaptor;
    private final OrderSheetUserMapper mapper;

    public OrderSheetUserResult.Profile getUserProfile(Long userId) {
        UserClientResponse.Profile profile = fetchUserProfileWithTranslation(userId);
        return mapper.toResult(profile);
    }

    public OrderSheetUserResult.UserPoint getUserPoints(Long userId, Money orderAmount) {
        UserClientResponse.UserPoints userPoints = fetchUserPointsWithTranslation(userId, orderAmount.longValue());
        return mapper.toResult(userPoints);
    }

    public OrderSheetUserResult.UserPoint getUserPointsForOrder(Long userId, Money orderAmount, Money usedPoints) {
        UserClientResponse.UserPoints userPoints = fetchUserPointsForOrderWithTranslation(userId, orderAmount.longValue(), usedPoints.longValue());
        return mapper.toResult(userPoints);
    }

    private UserClientResponse.Profile fetchUserProfileWithTranslation(Long userId) {
        try {
            return userAdaptor.getUserProfile(userId);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

    private UserClientResponse.UserPoints fetchUserPointsWithTranslation(Long userId, Long orderAmount) {
        try {
            return userAdaptor.getUserPoints(userId, orderAmount);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        } catch (ExternalServerException e){
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }

    private UserClientResponse.UserPoints fetchUserPointsForOrderWithTranslation(Long userId, Long orderAmount, Long usedPoints) {
        try {
            return userAdaptor.getUserPointsForOrder(userId, orderAmount, usedPoints);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
