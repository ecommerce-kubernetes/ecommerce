package com.example.order_service.ordersheet.application.external;

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
}
