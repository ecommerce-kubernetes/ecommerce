package com.example.order_service.ordersheet.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.ordersheet.application.dto.command.OrderSheetCommand;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetCouponResult;
import com.example.order_service.ordersheet.application.mapper.OrderSheetCouponMapper;
import com.example.order_service.ordersheet.exception.OrderSheetErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderSheetCouponGatewayTest {

    @InjectMocks
    private OrderSheetCouponGateway orderSheetCouponGateway;
    @Mock
    private CouponAdaptor adaptor;
    @Mock
    private OrderSheetCouponMapper couponMapper;

    @Nested
    @DisplayName("쿠폰 검증")
    class Calculate {
        @Test
        @DisplayName("쿠폰 정보를 조회한다")
        void calculate() {
            //given
            OrderSheetCommand.CouponCalculate command = fixtureMonkey.giveMeOne(OrderSheetCommand.CouponCalculate.class);
            CouponClientResponse.Calculate response = fixtureMonkey.giveMeOne(CouponClientResponse.Calculate.class);
            OrderSheetCouponResult.Calculate result = fixtureMonkey.giveMeOne(OrderSheetCouponResult.Calculate.class);
            given(adaptor.calculate(any())).willReturn(response);
            given(couponMapper.toResult(any())).willReturn(result);
            //when
            OrderSheetCouponResult.Calculate calculate = orderSheetCouponGateway.calculate(command);
            //then
            assertThat(calculate).isNotNull();
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 비지니스 예외가 발생한다")
        void calculate_ExternalServerException() {
            //given
            OrderSheetCommand.CouponCalculate command = fixtureMonkey.giveMeOne(OrderSheetCommand.CouponCalculate.class);
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "처리중 오류가 발생했습니다"))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderSheetCouponGateway.calculate(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_COUPON_SERVER_ERROR);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 비지니스 예외가 발생한다")
        void calculate_ExternalClientException() {
            //given
            OrderSheetCommand.CouponCalculate command = fixtureMonkey.giveMeOne(OrderSheetCommand.CouponCalculate.class);
            willThrow(new ExternalClientException("COUPON_EXPIRED", "쿠폰이 만료되었습니다"))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderSheetCouponGateway.calculate(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_COUPON_CLIENT_ERROR);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 사용 불가 오류가 발생한 경우 비지니스 예외가 발생한다")
        void calculate_ExternalUnavailableServerException() {
            //given
            OrderSheetCommand.CouponCalculate command = fixtureMonkey.giveMeOne(OrderSheetCommand.CouponCalculate.class);
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "쿠폰 서비스 통신 장애"))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderSheetCouponGateway.calculate(command))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderSheetErrorCode.ORDER_SHEET_COUPON_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
