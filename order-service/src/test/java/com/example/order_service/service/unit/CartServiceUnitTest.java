package com.example.order_service.service.unit;

import com.example.order_service.common.MessagePath;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.CartService;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.order_service.common.MessagePath.PRODUCT_VARIANT_NOT_FOUND;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @InjectMocks
    CartService cartService;

    @Mock
    CartsRepository cartsRepository;

    @Mock
    ProductClientService productClientService;
    @Captor
    ArgumentCaptor<Carts> cartsArgumentCaptor;

    @Test
    @DisplayName("장바구니 추가 테스트-성공(새로운 상품 추가)")
    void addItemTest_unit_success_newItem(){
        when(productClientService.fetchProductByVariantId(1L))
                .thenReturn(
                        new ProductResponse(1L, 1L, "상품1", 3000, 10, "http://product1.jpg",
                                List.of(new ItemOptionResponse("색상", "RED")))
                );
        when(cartsRepository.findByUserId(1L))
                .thenReturn(Optional.empty());
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
        verify(cartsRepository).save(cartsArgumentCaptor.capture());
        Carts savedCart = cartsArgumentCaptor.getValue();
        assertThat(savedCart.getCartItems()).hasSize(1);
        assertThat(savedCart.getCartItems())
                .extracting(CartItems::getProductVariantId, CartItems::getQuantity)
                .containsExactlyInAnyOrder(
                        tuple(1L, 10)
                );
    }

    @Test
    @DisplayName("장바구니 추가 테스트-성공(기존에 존재하는 상품 추가)")
    void addItemTest_unit_success_existItem(){

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
