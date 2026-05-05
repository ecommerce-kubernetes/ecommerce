package com.example.order_service.infrastructure.adaptor;

import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.client.UserFeignClient;
import com.example.order_service.infrastructure.dto.response.UserClientResponse;
import com.example.order_service.support.annotation.IsolatedTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.example.order_service.support.TestFixtureUtil.giveMeOne;
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
    @MockitoBean
    private ExternalExceptionTranslator translator;

    @Test
    @DisplayName("유저 서비스에 주문에 필요한 유저 정보를 조회한다")
    void getUserInfoForOrder(){
        //given
        Long userId = 1L;
        UserClientResponse.UserInfo mockResponse = giveMeOne(UserClientResponse.UserInfo.class);
        given(client.getUserInfoForOrder(anyLong()))
                .willReturn(mockResponse);
        //when
        UserClientResponse.UserInfo response = userAdaptor.getUserInfoForOrder(userId);
        //then
        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(mockResponse);
    }

    @Test
    @DisplayName("유저 조회중 예외 발생시 translator를 호출하여 반환된 예외를 던진다")
    void getUserInfoForOrder_fallback_delegate_to_translator() throws Throwable {
        //given
        //발생한 예외
        RuntimeException feignException = new RuntimeException("feignClient 예외");
        //변환된 예외
        ExternalSystemUnavailableException translatedException =
                new ExternalSystemUnavailableException("CODE", "변환된 에러", feignException);
        willThrow(feignException).given(client).getUserInfoForOrder(anyLong());
        given(translator.translate(anyString(), any(Throwable.class)))
                .willReturn(translatedException);
        //when
        //then
        assertThatThrownBy(() -> userAdaptor.getUserInfoForOrder(anyLong()))
                .isInstanceOf(ExternalSystemUnavailableException.class);
    }
}
