package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.order.application.dto.result.OrderCouponResult;
import com.example.order_service.order.application.mapper.OrderCouponMapper;
import com.example.order_service.order.domain.model.vo.CouponValidationStatus;
import com.example.order_service.order.exception.OrderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
public class OrderCouponGatewayTest {

    @InjectMocks
    private OrderCouponGateway orderCouponGateway;

    @Mock
    private CouponAdaptor adaptor;
    @Spy
    private OrderCouponMapper couponMapper = Mappers.getMapper(OrderCouponMapper.class);

    @Nested
    @DisplayName("쿠폰 정보 조회")
    class GetCoupon {

        @Test
        @DisplayName("쿠폰 정보를 조회한다")
        void getCoupon(){
            //given
            CouponClientResponse.Calculate response = fixtureMonkey.giveMeBuilder(CouponClientResponse.Calculate.class)
                    .set("code", "SUCCESS").sample();
            given(adaptor.calculate(any()))
                    .willReturn(response);
            //when
            OrderCouponResult.CouponValidation result = orderCouponGateway.calculateCouponDiscount(1L, 1L, 10000L);
            //then
            assertThat(result.status()).isEqualTo(CouponValidationStatus.SUCCESS);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 비지니스 예외로 변경하여 던진다")
        void getCoupon_ExternalServerException(){
            //given
            willThrow(new ExternalServerException("INTERNAL_SERVER_ERROR", "처리중 오류가 발생했습니다"))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderCouponGateway.calculateCouponDiscount(1L, 1L, 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_COUPON_SERVER_ERROR);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 비지니스 예외로 변환해 던진다")
        void getCoupon_ExternalClientException(){
            //given
            willThrow(new ExternalClientException("NOT_FOUND_COUPON", "쿠폰을 찾을 수 없습니다"))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderCouponGateway.calculateCouponDiscount(1L, 1L, 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR);
        }

        @Test
        @DisplayName("쿠폰 조회중 서비스 불가 오류가 발생한 경우 비지니스 예외로 변환해 던진다")
        void getCoupon_ExternalUnavailableException(){
            //given
            willThrow(new ExternalSystemUnavailableException("SERVICE_UNAVAILABLE", "쿠폰 서비스 통신장애"))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderCouponGateway.calculateCouponDiscount(1L, 1L, 10000L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(OrderErrorCode.ORDER_COUPON_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
