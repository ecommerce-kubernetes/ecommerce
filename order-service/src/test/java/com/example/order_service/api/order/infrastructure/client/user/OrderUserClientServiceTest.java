package com.example.order_service.api.order.infrastructure.client.user;

import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.ExternalServiceErrorCode;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import com.example.order_service.api.support.ExcludeInfraTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

public class OrderUserClientServiceTest extends ExcludeInfraTest {

    @Autowired
    private OrderUserClientService orderUserClientService;
    @MockitoBean
    private OrderUserClient orderUserClient;

    @Test
    @DisplayName("유저 서비스에서 유저 정보를 조회한다")
    void getUserForOrder() {
        //given
        OrderUserResponse response = OrderUserResponse.builder()
                .userId(1L)
                .pointBalance(2000L)
                .build();

        given(orderUserClient.getOrderInfo(anyLong()))
                .willReturn(response);
        //when
        OrderUserResponse result = orderUserClientService.getUserForOrder(1L);
        //then
        assertThat(result)
                .extracting(OrderUserResponse::getUserId, OrderUserResponse::getPointBalance)
                .contains(1L, 2000L);
    }

    @Test
    @DisplayName("서킷브레이커가 열렸을때 유저 정보를 조회하면 예외를 던진다")
    void getUserForOrder_When_OpenCircuitBreaker() {
        //given
        willThrow(CallNotPermittedException.class)
                .given(orderUserClient).getOrderInfo(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderUserClientService.getUserForOrder(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.UNAVAILABLE);
    }

    @Test
    @DisplayName("유저 정보를 조회할때 유저를 찾을 수 없는 경우 받은 예외를 그대로 던진다")
    void getUserForOrder_When_NotFound_Exception() {
        //given
        willThrow(new BusinessException(ExternalServiceErrorCode.USER_NOT_FOUND))
                .given(orderUserClient).getOrderInfo(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderUserClientService.getUserForOrder(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("서버 에러 응답이 온 경우 예외를 던진다")
    void getUserForOrder_When_Unknown_Exception() {
        //given
        willThrow(new RuntimeException("유저 서비스 오류 발생"))
                .given(orderUserClient).getOrderInfo(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderUserClientService.getUserForOrder(1L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ExternalServiceErrorCode.SYSTEM_ERROR);
    }
}
