package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.dto.command.OrderCommand;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import com.example.order_service.order.application.mapper.OrderCouponMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCouponGateway {
    private final CouponAdaptor couponAdaptor;
    private final OrderCouponMapper mapper;

    public OrderCouponResult.Calculate calculate(OrderCommand.CouponCalculate command) {
        CouponCommand.Calculate couponCommand = mapper.toCommand(command);
        CouponClientResponse.Calculate response = fetchCouponWithTranslation(couponCommand);
        return mapper.toResult(response);
    }

    private CouponClientResponse.Calculate fetchCouponWithTranslation(CouponCommand.Calculate command) {
        try {
            return couponAdaptor.calculate(command);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderErrorCode.ORDER_COUPON_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderErrorCode.ORDER_USER_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
