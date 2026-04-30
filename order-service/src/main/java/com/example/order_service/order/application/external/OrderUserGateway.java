package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.business.code.OrderErrorCode;
import com.example.order_service.order.domain.service.dto.result.OrderUserInfo;
import com.example.order_service.order.infrastructure.client.user.OrderUserAdaptor;
import com.example.order_service.order.infrastructure.client.user.dto.OrderUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderUserGateway {
    private final OrderUserAdaptor orderUserAdaptor;

    public OrderUserInfo getUser(Long userId, Long pointToUse) {
        OrderUserResponse user = orderUserAdaptor.getUser(userId);
        validateEnoughPoint(user, pointToUse);
        return mapToInfo(user);
    }

    private void validateEnoughPoint(OrderUserResponse user, Long pointToUse) {
        if (user.getPointBalance() < pointToUse) {
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
