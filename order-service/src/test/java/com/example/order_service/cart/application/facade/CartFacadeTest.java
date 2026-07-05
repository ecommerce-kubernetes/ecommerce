package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.cart.application.dto.result.CartResult;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartFacadeTest {

    @InjectMocks
    private CartFacade cartFacade;
    @Mock
    private CartProductGateway cartProductGateway;
    @Mock
    private CartCommandService cartCommandService;
    @Mock
    private CartQueryService cartQueryService;
    @Mock
    private CartItemValidator validator;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {

        @Test
        @DisplayName("상품을 조회하여 상품 검증 후 장바구니에 추가한다")
        void addItem() {
            //given
            AddCartItemsCommand.Item item = Instancio.of(AddCartItemsCommand.Item.class)
                    .set(field("productVariantId"), 1L)
                    .create();
            AddCartItemsCommand command = Instancio.of(AddCartItemsCommand.class)
                    .set(field("items"), List.of(item))
                    .create();
            CartProductResult product = Instancio.of(CartProductResult.class)
                    .set(field("productVariantId"), item.productVariantId())
                    .set(field("status"), CartProductStatus.ON_SALE)
                    .set(field("stock"), item.quantity() + 100)
                    .create();
            CartProductListResult productList = Instancio.of(CartProductListResult.class)
                    .set(field("products"), List.of(product))
                    .create();
            CartItemData savedItem = Instancio.of(CartItemData.class)
                    .set(field("productVariantId"), item.productVariantId())
                    .set(field("quantity"), item.quantity())
                    .create();
            given(cartProductGateway.getProducts(anyList())).willReturn(productList);
            doNothing().when(validator).validate(any(AddCartItemsCommand.class), any());
            doNothing().when(cartCommandService).addCartItems(any(AddCartItemsCommand.class));
            given(cartQueryService.getCartItems(anyLong())).willReturn(List.of(savedItem));
            //when
            CartResult cartResult = cartFacade.addItems(command);
            //then
            assertThat(cartResult.items()).hasSize(1);
            assertThat(cartResult.items())
                    .extracting("status", "productVariantId")
                    .containsExactly(
                            tuple(CartItemAvailability.AVAILABLE, item.productVariantId())
                    );
        }
    }
}
