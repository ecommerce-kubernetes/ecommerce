package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetCouponMapper;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderSheetCouponGateway {
    private final CouponAdaptor couponAdaptor;
    private final OrderSheetCouponMapper mapper;

    public List<OrderSheetCouponResult.Info> getCoupons(OrderSheet orderSheet) {
        return null;
    }

    private List<CouponClientResponse.CouponInfo> fetchCouponsWithTranslation(CouponCommand.CouponEvaluate command) {
        try {
            return couponAdaptor.evaluate(command);
        } catch (ExternalClientException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_COUPON_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_COUPON_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            throw new BusinessException(OrderSheetErrorCode.ORDER_SHEET_COUPON_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
