package com.example.order_service.api.order.domain.service;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.OrderErrorCode;
import com.example.order_service.api.order.domain.service.dto.result.OrderUserInfo;
import com.example.order_service.api.order.infrastructure.client.user.OrderUserAdaptor;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderUserService {
    private final OrderUserAdaptor orderUserAdaptor;

    public OrderUserInfo getUser(Long userId, Long useToPoint) {
        OrderUserResponse user = orderUserAdaptor.getUser(userId);
        validateEnoughPoint(user, useToPoint);
        return mapToInfo(user);
    }

    private void validateEnoughPoint(OrderUserResponse user, Long useToPoint) {
        if (user.getPointBalance() < useToPoint) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_INSUFFICIENT_POINT_BALANCE);
        }
    }

    private OrderUserInfo mapToInfo(OrderUserResponse user) {
        return OrderUserInfo.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
