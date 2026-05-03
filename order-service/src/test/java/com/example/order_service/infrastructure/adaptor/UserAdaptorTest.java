package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.UserFeignClient;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.example.order_service.support.TestFixtureUtil.giveMeOne;
import static io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@IsolatedTest
public class UserAdaptorTest {
    @Autowired
    private UserAdaptor userAdaptor;
    @MockitoBean
    private UserFeignClient client;

    @Test
    @DisplayName("유저 서비스에 주문에 필요한 유저 정보를 조회한다")
    void getUserInfoForOrder(){
        //given
        Long userId = 1L;
        UserClientResponse.OrderUserInfo mockResponse = giveMeOne(UserClientResponse.OrderUserInfo.class);
        given(client.getUserInfoForOrder(anyLong()))
                .willReturn(mockResponse);
        //when
        UserClientResponse.OrderUserInfo response = userAdaptor.getUserInfoForOrder(userId);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("유저 서비스에 유저 정보를 조회할때 서킷 브레이커가 열렸다면 시스템 예외로 변환하여 던진다")
    void getUserInfoForOrder_circuitbreaker_open() {
        //given
        Long userId = 1L;
        CallNotPermittedException circuitException = CallNotPermittedException
                .createCallNotPermittedException(ofDefaults("test"));
        willThrow(circuitException).given(client)
                .getUserInfoForOrder(anyLong());
        //when
        //then
        assertThatThrownBy(() -> userAdaptor.getUserInfoForOrder(userId))
                .isInstanceOf(ExternalSystemUnavailableException.class)
                .hasMessage("CircuitBreaker Open");
    }

    @Test
    @DisplayName("상품 서비스에서 상품을 조회할때 external System 예외가 던져지면 그대로 던진다")
    void getUserInfoForOrder_external_system_exception(){
        //given
        Long userId = 1L;
        willThrow(ExternalSystemException.class).given(client)
                .getUserInfoForOrder(anyLong());
        //when
        //then
        assertThatThrownBy(() -> userAdaptor.getUserInfoForOrder(userId))
                .isInstanceOf(ExternalSystemException.class);
    }

    @Test
    @DisplayName("")
    void getUserInfoForOrder_other_exception(){
        //given
        Long userId = 1L;
        willThrow(RuntimeException.class).given(client)
                .getUserInfoForOrder(anyLong());
        //when
        //then
        assertThatThrownBy(() -> userAdaptor.getUserInfoForOrder(userId))
                .isInstanceOf(ExternalSystemException.class);
    }
}
