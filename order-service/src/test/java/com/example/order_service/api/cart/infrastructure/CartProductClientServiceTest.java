package com.example.order_service.api.cart.infrastructure;

import com.example.order_service.api.cart.infrastructure.client.CartProductClient;
import com.example.order_service.api.cart.infrastructure.client.CartProductClientService;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.cart.infrastructure.client.dto.ItemOption;
import com.example.order_service.api.cart.infrastructure.client.dto.UnitPrice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CartProductClientServiceTest {

    @InjectMocks
    private CartProductClientService cartProductClientService;

    @Mock
    private CartProductClient cartProductClient;

    @Test
    @DisplayName("상품 서비스에서 상품의 정보를 조회한다")
    void getProduct(){
        //given
        CartProductResponse product = CartProductResponse.builder()
                .productId(1L)
                .productVariantId(1L)
                .productName("상품1")
                .unitPrice(
                        UnitPrice.builder()
                                .originalPrice(3000)
                                .discountRate(10)
                                .discountAmount(300)
                                .discountedPrice(2700)
                                .build()
                )
                .thumbnailUrl("http://thumbnail.jpg")
                .itemOptions(
                        List.of(
                                ItemOption.builder()
                                        .optionTypeName("사이즈")
                                        .optionValueName("XL")
                                        .build()
                        )
                )
                .build();

        given(cartProductClient.getProductByVariantId(anyLong()))
                .willReturn(product);
        //when
        CartProductResponse response = cartProductClientService.getProduct(1L);
        //then
        assertThat(response)
                .extracting("productId", "productVariantId", "productName", "thumbnailUrl")
                .contains(1L, 1L, "상품1", "http://thumbnail.jpg");

        assertThat(response.getUnitPrice())
                .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                .contains(3000L, 10, 300L, 2700L);
    }
}
