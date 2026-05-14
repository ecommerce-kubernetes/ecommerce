package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetCouponMapper;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSheetCouponGateway {
    private final CouponAdaptor couponAdaptor;
    private final OrderSheetCouponMapper mapper;

    public OrderSheetCouponResult.Calculate calculate(OrderSheetCommand.CouponCalculate command) {
        CouponCommand.Calculate couponCommand = mapper.toCommand(command);
        CouponClientResponse.Calculate response = fetchCouponWithTranslation(couponCommand);
        return mapper.toResult(response);
    }

    private CouponClientResponse.Calculate fetchCouponWithTranslation(CouponCommand.Calculate command) {
        try {
            return couponAdaptor.calculate(command);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_COUPON_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_COUPON_SERVER_ERROR);
        }catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_COUPON_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
