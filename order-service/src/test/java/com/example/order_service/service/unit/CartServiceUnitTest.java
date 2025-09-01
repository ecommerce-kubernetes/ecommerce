package com.example.order_service.service.unit;

import com.example.order_service.common.MessagePath;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.CartService;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.util.TestMessageUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static com.example.order_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @InjectMocks
    CartService cartService;

    @Mock
    CartsRepository cartsRepository;

    @Mock
    ProductClientService productClientService;

    @Test
    @DisplayName("장바구니 추가 테스트-성공")
    void addItemTest_unit_success(){
        CartItemRequest request = new CartItemRequest(1L, 10);
        CartItemResponse response = cartService.addItem(1L, request);

        assertThat(response.getProductId()).isEqualTo(1L);
        assertThat(response.getProductVariantId()).isEqualTo(1L);
        assertThat(response.getProductName()).isEqualTo("상품1");
        assertThat(response.getThumbNailUrl()).isEqualTo("http://product1.jpg");
        assertThat(response.getPrice()).isEqualTo(3000);
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getDiscountRate()).isEqualTo(10);

        assertThat(response.getOptions())
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(
                        tuple("색상", "RED")
                );
    }

    @Test
    @DisplayName("장바구니 추가 테스트-실패(장바구니에 추가하려는 상품이 없는 경우)")
    void addItemTest_unit_notFoundProductVariant(){
        when(productClientService.fetchProductByVariantId(1L))
                .thenThrow(new NotFoundException(getMessage(PRODUCT_VARIANT_NOT_FOUND)));

        CartItemRequest request = new CartItemRequest(1L, 10);
        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_NOT_FOUND));
    }

}
