package com.example.order_service.api.order.infrastructure.client.user;

import com.example.order_service.api.common.exception.NotFoundException;
import com.example.order_service.api.common.exception.server.InternalServerException;
import com.example.order_service.api.common.exception.server.UnavailableServiceException;
import com.example.order_service.api.order.infrastructure.client.user.dto.OrderUserResponse;
import com.example.order_service.api.support.ExcludeInfraServiceTest;
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

public class OrderUserClientServiceTest extends ExcludeInfraServiceTest {

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
                .extracting("userId", "pointBalance")
                .contains(1L, 2000L);
    }

    @Test
    @DisplayName("서킷브레이커가 열리면 예외를 던진다")
    void getUserForOrderWhenOpenCircuitBreaker() {
        //given
        willThrow(CallNotPermittedException.class)
                .given(orderUserClient).getOrderInfo(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderUserClientService.getUserForOrder(1L))
                .isInstanceOf(UnavailableServiceException.class)
                .hasMessage("유저 서비스가 응답하지 않습니다");
    }

    @Test
    @DisplayName("NotFound 에러 응답이 온 경우 예외를 던진다")
    void getUserForOrderWhenNotFoundException() {
        //given
        willThrow(new NotFoundException("유저를 찾을 수 없습니다"))
                .given(orderUserClient).getOrderInfo(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderUserClientService.getUserForOrder(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("유저를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("서버 에러 응답이 온 경우 예외를 던진다")
    void getUserForOrderWhenServerError() {
        //given
        willThrow(new InternalServerException("유저 서비스 오류 발생"))
                .given(orderUserClient).getOrderInfo(anyLong());
        //when
        //then
        assertThatThrownBy(() -> orderUserClientService.getUserForOrder(1L))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("유저 서비스 오류 발생");
    }
}
