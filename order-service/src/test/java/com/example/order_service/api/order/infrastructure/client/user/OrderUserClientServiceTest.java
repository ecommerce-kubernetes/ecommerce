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
    @DisplayName("유저 정보를 조회할때 비지니스 예외가 발생하면 그대로 던진다")
    void getUserForOrder_When_BusinessException() {
        //given
        willThrow(BusinessException.class)
                .given(orderUserClient).getOrderInfo(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderUserClientService.getUserForOrder(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("알 수 없는 예외를 받은 경우 서버 예외로 변환해 던진다")
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
