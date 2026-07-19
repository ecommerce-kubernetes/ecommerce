package com.example.order_service.order.application.external;

import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.CouponAdaptor;
import com.example.order_service.infrastructure.dto.command.CouponCommand;
import com.example.order_service.infrastructure.dto.response.CouponClientResponse;
import com.example.order_service.infrastructure.dto.response.coupon.ItemCouponResponse;
import com.example.order_service.order.application.external.dto.command.OrderCouponCommand;
import com.example.order_service.order.application.external.dto.result.ItemCouponResult;
import com.example.order_service.order.application.external.dto.result.OrderCouponResult;
import com.example.order_service.order.application.external.mapper.OrderCouponMapper;
import com.example.order_service.order.domain.policy.FixedCouponDiscountPolicy;
import com.example.order_service.order.exception.OrderErrorCode;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @Mock
    private OrderCouponMapper couponMapper;

    @Test
    @DisplayName("상품 쿠폰을 조회한다.")
    void getItemCoupon(){
        //given
        Long userId = 1L;
        Long itemCouponId = 1L;

        ItemCouponResponse response = ItemCouponResponse.builder()
                .userId(userId)
                .discountType(ItemCouponResponse.DiscountType.FIXED)
                .itemCouponId(itemCouponId)
                .name("청바지 1000원 할인")
                .applyQuantityLimit(1)
                .discountAmount(1000L)
                .build();

        given(adaptor.getItemCoupon(anyLong(), anyLong())).willReturn(response);

        //when
        ItemCouponResult result = orderCouponGateway.getItemCoupon(userId, itemCouponId);
        //then
        assertThat(result.itemCoupon())
                .extracting("itemCouponId", "name", "applyQuantityLimit")
                .containsExactly(itemCouponId, response.name(), response.applyQuantityLimit());

        assertThat(result.itemCoupon().getDiscountPolicy())
                .isExactlyInstanceOf(FixedCouponDiscountPolicy.class);
    }

    @Test
    @DisplayName("쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 예외가 발생한다")
    void getItemCoupon_ExternalServerException(){
        //given
        String code = "INTERNAL_SERVER_ERROR";
        String message = "처리중 오류가 발생했습니다";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalServerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponGateway.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(OrderErrorCode.ORDER_COUPON_SERVER_ERROR, code);
    }

    @Test
    @DisplayName("쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 예외가 발생한다")
    void getItemCoupon_ExternalClientException(){
        //given
        String code = "COUPON_EXPIRED";
        String message = "쿠폰이 만료되었습니다";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalClientException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponGateway.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR, code);
    }

    @Test
    @DisplayName("쿠폰 조회중 쿠폰 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
    void calculate_ExternalCircuitBreakerException(){
        //given
        String code = "COUPON_CIRCUIT_OPEN";
        String message = "쿠폰 서비스 서킷 브레이커 열림";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalCircuitBreakerException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponGateway.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(OrderErrorCode.ORDER_COUPON_CIRCUIT_OPEN, code);
    }

    @Test
    @DisplayName("쿠폰 조회중 쿠폰 서비스에서 통신 불가 오류가 발생한 경우 예외가 발생한다")
    void calculate_ExternalUnavailableServerException() {
        //given
        String code = "SERVICE_UNAVAILABLE";
        String message = "쿠폰 서비스 통신 장애";
        given(adaptor.getItemCoupon(anyLong(), anyLong())).willThrow(new ExternalSystemUnavailableException(code, message));
        //when
        //then
        assertThatThrownBy(() -> orderCouponGateway.getItemCoupon(1L, 1L))
                .isInstanceOf(DefaultGatewayException.class)
                .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                .extracting("errorCode", "externalErrorCode")
                .containsExactly(OrderErrorCode.ORDER_COUPON_UNAVAILABLE_SERVER_ERROR, code);
    }

    @Nested
    @DisplayName("쿠폰 검증")
    class Calculate {
        @Test
        @DisplayName("쿠폰 정보를 조회한다")
        void calculate() {
            //given
            OrderCouponCommand.Calculate command = Instancio.create(OrderCouponCommand.Calculate.class);
            CouponCommand.Calculate couponCommand = Instancio.create(CouponCommand.Calculate.class);
            CouponClientResponse.Calculate response = Instancio.create(CouponClientResponse.Calculate.class);
            OrderCouponResult.Calculate result = Instancio.create(OrderCouponResult.Calculate.class);
            given(couponMapper.toCommand(any())).willReturn(couponCommand);
            given(adaptor.calculate(any())).willReturn(response);
            given(couponMapper.toResult(any())).willReturn(result);
            //when
            OrderCouponResult.Calculate calculate = orderCouponGateway.calculate(command);
            //then
            assertThat(calculate).isEqualTo(result);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 서버 오류가 발생한 경우 예외가 발생한다")
        void calculate_ExternalServerException() {
            //given
            String code = "INTERNAL_SERVER_ERROR";
            String message = "처리중 오류가 발생했습니다";
            OrderCouponCommand.Calculate command = Instancio.create(OrderCouponCommand.Calculate.class);
            willThrow(new ExternalServerException(code, message))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderCouponGateway.calculate(command))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_COUPON_SERVER_ERROR, code);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 클라이언트 오류가 발생한 경우 예외가 발생한다")
        void calculate_ExternalClientException() {
            //given
            String code = "COUPON_EXPIRED";
            String message = "쿠폰이 만료되었습니다";
            OrderCouponCommand.Calculate command = Instancio.create(OrderCouponCommand.Calculate.class);
            willThrow(new ExternalClientException(code, message))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderCouponGateway.calculate(command))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_COUPON_CLIENT_ERROR, code);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스 서킷 브레이커가 열린 경우 예외가 발생한다")
        void calculate_ExternalCircuitBreakerException(){
            //given
            String code = "COUPON_CIRCUIT_OPEN";
            String message = "쿠폰 서비스 서킷 브레이커 열림";
            OrderCouponCommand.Calculate command = Instancio.create(OrderCouponCommand.Calculate.class);
            willThrow(new ExternalCircuitBreakerException(code, message))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderCouponGateway.calculate(command))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_COUPON_CIRCUIT_OPEN, code);
        }

        @Test
        @DisplayName("쿠폰 조회중 쿠폰 서비스에서 사용 불가 오류가 발생한 경우 예외가 발생한다")
        void calculate_ExternalUnavailableServerException() {
            //given
            String code = "SERVICE_UNAVAILABLE";
            String message = "쿠폰 서비스 통신 장애";
            OrderCouponCommand.Calculate command = Instancio.create(OrderCouponCommand.Calculate.class);
            willThrow(new ExternalSystemUnavailableException(code, message))
                    .given(adaptor).calculate(any());
            //when
            //then
            assertThatThrownBy(() -> orderCouponGateway.calculate(command))
                    .isInstanceOf(DefaultGatewayException.class)
                    .hasMessage(String.format("Gateway Error: [%s] %s", code, message))
                    .extracting("errorCode", "externalErrorCode")
                    .containsExactly(OrderErrorCode.ORDER_COUPON_UNAVAILABLE_SERVER_ERROR, code);
        }
    }
}
