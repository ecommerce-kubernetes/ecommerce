package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.infrastructure.client.CartProductAdaptor;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.example.order_service.api.support.fixture.cart.CartProductFixture.anCartProductInfo;
import static com.example.order_service.api.support.fixture.cart.CartProductFixture.anCartProductResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Nested
    @DisplayName("상품 목록 정보 조회")
    class GetProductInfos {

        @Test
        @DisplayName("상품 목록 정보를 조회한다")
        void getProductInfos(){
            //given
            CartProductResponse item1 = anCartProductResponse().productVariantId(1L).status("ON_SALE").build();
            CartProductResponse item2 = anCartProductResponse().productVariantId(2L).status("DELETED").build();
            CartProductResponse item3 = anCartProductResponse().productVariantId(3L).status("STOP_SALE").build();
            CartProductResponse item4 = anCartProductResponse().productVariantId(4L).status("PREPARING").build();

            CartProductInfo expectedResult1 = anCartProductInfo().productVariantId(1L).status(ProductStatus.ON_SALE).build();
            CartProductInfo expectedResult2 = anCartProductInfo().productVariantId(2L).status(ProductStatus.DELETED).build();
            CartProductInfo expectedResult3 = anCartProductInfo().productVariantId(3L).status(ProductStatus.STOP_SALE).build();
            CartProductInfo expectedResult4 = anCartProductInfo().productVariantId(4L).status(ProductStatus.PREPARING).build();
            given(adaptor.getProducts(anyList())).willReturn(List.of(item1, item2, item3, item4));
            //when
            List<CartProductInfo> result = cartProductService.getProductInfos(List.of(1L, 2L, 3L, 4L, 5L));
            //then
            assertThat(result)
                    .usingRecursiveComparison()
                    .isEqualTo(List.of(expectedResult1, expectedResult2, expectedResult3, expectedResult4));
        }
    }
}
