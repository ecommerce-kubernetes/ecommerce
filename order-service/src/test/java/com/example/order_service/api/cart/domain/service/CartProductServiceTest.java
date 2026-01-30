package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.infrastructure.client.CartProductAdaptor;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import com.example.order_service.api.support.fixture.cart.CartProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.example.order_service.api.support.fixture.cart.CartProductFixture.anCartProductInfo;
import static com.example.order_service.api.support.fixture.cart.CartProductFixture.anCartProductResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CartProductServiceTest {

    @InjectMocks
    private CartProductService cartProductService;
    @Mock
    private CartProductAdaptor adaptor;

    @Nested
    @DisplayName("상품 정보 조회")
    class GetProductInfo {

        @Test
        @DisplayName("장바구니 상품 정보를 조회한다")
        void getProductInfo() {
            //given
            CartProductResponse response = anCartProductResponse().build();
            CartProductInfo expectedResult = anCartProductInfo().build();
            given(adaptor.getProduct(anyLong()))
                    .willReturn(response);
            //when
            CartProductInfo result = cartProductService.getProductInfo(1L);
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("상품이 판매중이 아닌 경우 예외를 던진다")
        void getProductInfo_product_status_not_on_sale() {
            //given
            CartProductResponse response = anCartProductResponse()
                    .status("DELETED").build();
            given(adaptor.getProduct(anyLong()))
                    .willReturn(response);
            //when
            //then
            assertThatThrownBy(() -> cartProductService.getProductInfo(1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }
    }
}
