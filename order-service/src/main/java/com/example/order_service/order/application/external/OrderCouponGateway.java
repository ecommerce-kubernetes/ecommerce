package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.application.BusinessException;
import com.example.order_service.common.exception.application.DefaultGatewayException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.mapper.OrderCouponMapper;
import com.example.order_service.order.exception.OrderErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 주문 쿠폰 도메인 통신을 담당하는 Gateway 서비스
 * <p>
 * 쿠폰 도메인의 응답을 서비스 레이어의 Result 로 매핑하여 반환
 * 쿠폰 도메인 통신중 발생하는 예외를 비지니스 예외로 변환
 * </p>
 *
 * @author 최민식
 * @since 2026. 05. 22
 */
@Service
@RequiredArgsConstructor
public class OrderCouponGateway {
    private final CouponAdaptor couponAdaptor;
    private final OrderCouponMapper mapper;

    /**
     * 쿠폰 도메인에 쿠폰 적용 상품 정보를 요청하여 쿠폰 할인금 정보를 반환
     *
     * @param command 쿠폰 적용 상품 정보
     * @return 쿠폰의 할인금 결과를 반환
     * @throws BusinessException 쿠폰 도메인 통신중 발생한 예외를 비지니스 예외로 변환
     */
    public OrderCouponResult.Calculate calculate(OrderCouponCommand.Calculate command) {
        CouponCommand.Calculate couponCommand = mapper.toCommand(command);
        CouponClientResponse.Calculate response = fetchCouponWithTranslation(couponCommand);
        return mapper.toResult(response);
    }

    private CouponClientResponse.Calculate fetchCouponWithTranslation(CouponCommand.Calculate command) {
        try {
            return couponAdaptor.calculate(command);
        } catch (ExternalClientException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultGatewayException(OrderErrorCode.ORDER_COUPON_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        }
    }
}
