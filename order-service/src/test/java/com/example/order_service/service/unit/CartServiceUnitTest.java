package com.example.order_service.service.unit;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.ProductInfo;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartItemsRepository;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.CartService;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @InjectMocks
    CartService cartService;

    @Mock
    CartsRepository cartsRepository;
    @Mock
    CartItemsRepository cartItemsRepository;
    @Mock
    ProductClientService productClientService;

    @Mock
    MessageSourceUtil ms;

    @Test
    @DisplayName("장바구니 상품 추가 테스트-성공(처음 상품을 추가하는 경우)")
    void addItemTest_unit_success_newCart(){
        ProductResponse productResponse = new ProductResponse(1L, 1L, "상품1", 3000, 10, "http://product1.jpg",
                List.of(new ItemOptionResponse("색상", "RED")));
        when(productClientService.fetchProductByVariantId(1L))
                .thenReturn(productResponse);
        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.empty());
        when(cartsRepository.save(any(Carts.class)))
                .thenReturn(new Carts(1L));

        CartItemResponse response = cartService.addItem(1L, new CartItemRequest(1L, 10));

        assertThat(response)
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(10, true);

        assertThat(response.getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 1L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getProductInfo().getItemOptions())
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(tuple("색상", "RED"));

    }

    @Test
    @DisplayName("장바구니 상품 추가 테스트-성공(새로운 상품을 추가하는 경우)")
    void addItemTest_unit_success_newItem(){
        ProductResponse productResponse = new ProductResponse(1L, 1L, "상품1", 3000, 10, "http://product1.jpg",
                List.of(new ItemOptionResponse("색상", "RED")));

        Carts cart = new Carts(1L);
        when(productClientService.fetchProductByVariantId(1L))
                .thenReturn(productResponse);
        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.of(cart));

        CartItemResponse response = cartService.addItem(1L, new CartItemRequest(1L, 10));

        assertThat(response)
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(10, true);

        assertThat(response.getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 1L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getProductInfo().getItemOptions())
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(tuple("색상", "RED"));
    }

    @Test
    @DisplayName("장바구니 상품 추가 테스트-성공(장바구니에 이미 존재하는 상품인 경우)")
    void addItemTest_unit_success_existItem(){
        ProductResponse productResponse = new ProductResponse(1L, 1L, "상품1", 3000, 10, "http://product1.jpg",
                List.of(new ItemOptionResponse("색상", "RED")));
        Carts cart = new Carts(1L);
        CartItems cartItem = new CartItems(1L, 10);
        cart.addCartItem(cartItem);
        when(productClientService.fetchProductByVariantId(1L))
                .thenReturn(productResponse);
        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.of(cart));

        CartItemResponse response = cartService.addItem(1L, new CartItemRequest(1L, 5));

        assertThat(response)
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(15, true);

        assertThat(response.getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 1L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getProductInfo().getItemOptions())
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(tuple("색상", "RED"));
    }

    @Test
    @DisplayName("장바구니 상품 추가 테스트-실패(상품이 존재하지 않는 경우)")
    void addItemTest_unit_notFound_product(){
        doThrow(new NotFoundException(getMessage(PRODUCT_VARIANT_NOT_FOUND)))
                .when(productClientService).fetchProductByVariantId(1L);

        assertThatThrownBy(() -> cartService.addItem(1L, new CartItemRequest(1L, 3)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_NOT_FOUND));
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-장바구니 상품이 존재하는 경우")
    void getCartItemListTest_unit_success_existItem(){
        Carts cart = new Carts(1L);
        cart.addCartItem(new CartItems(1L, 2));
        cart.addCartItem(new CartItems(2L, 3));

        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(productClientService.fetchProductByVariantIds(List.of(1L,2L)))
                .thenReturn(
                        List.of(
                                new ProductResponse(1L, 1L, "상품1",
                                        3000, 10, "http://product1.jpg",
                                        List.of(new ItemOptionResponse("색상", "RED"))),
                                new ProductResponse(2L, 2L, "상품2", 5000, 5, "http://product2.jpg",
                                        List.of(new ItemOptionResponse("사이즈", "XL")))
                        )
                );
        CartResponse response = cartService.getCartItemList(1L);

        assertThat(response.getCartTotalPrice()).isEqualTo(19650);
        assertThat(response.getCartItems())
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(
                        tuple(2, true),
                        tuple(3, true)
                );

        assertThat(response.getCartItems().get(0).getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 1L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getCartItems().get(1).getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        2L, 2L, "상품2", 5000, 5, "http://product2.jpg"
                );
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-장바구니 상품중 삭제된 상품이 있는 경우")
    void getCartItemListTest_unit_success_deleted_product(){
        Carts cart = new Carts(1L);
        cart.addCartItem(new CartItems(1L, 2));
        cart.addCartItem(new CartItems(2L, 3));

        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.of(cart));
        when(productClientService.fetchProductByVariantIds(List.of(1L,2L)))
                .thenReturn(List.of(new ProductResponse(1L, 1L, "상품1",
                                        3000, 10, "http://product1.jpg",
                                List.of(new ItemOptionResponse("색상", "RED"))))
                );
        CartResponse response = cartService.getCartItemList(1L);
        assertThat(response.getCartTotalPrice()).isEqualTo(5400);

        assertThat(response.getCartItems().get(0).getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 1L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getCartItems().get(1).getProductInfo()).isNull();
        assertThat(response.getCartItems())
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(
                        tuple(2, true),
                        tuple(3, false)
                );
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-장바구니에 한번도 상품을 추가한적 없는 경우")
    void getCartItemListTest_unit_success_noCart(){
        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.empty());

        CartResponse response = cartService.getCartItemList(1L);
        assertThat(response.getCartItems()).isEmpty();
        assertThat(response.getCartTotalPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-장바구니에 상품이 없는경우")
    void getCartItemListTest_unit_success_noItem(){
        Carts cart = new Carts(1L);
        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.of(cart));

        CartResponse response = cartService.getCartItemList(1L);
        assertThat(response.getCartItems()).isEmpty();
        assertThat(response.getCartTotalPrice()).isEqualTo(0);
    }


    @Test
    @DisplayName("장바구니 상품 삭제 테스트-성공")
    void deleteCartItemByIdTest_unit_success(){
        Carts cart = new Carts(1L);
        CartItems cartItem = new CartItems(1L, 10);
        cart.addCartItem(cartItem);
        when(cartItemsRepository.findWithCartById(1L))
                .thenReturn(Optional.of(cartItem));

        cartService.deleteCartItemById(1L, 1L);

        assertThat(cart.getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 찾을 수 없을경우)")
    void deleteCartItemByIdTest_unit_notFoundCartItem(){
        when(cartItemsRepository.findWithCartById(1L)).thenReturn(Optional.empty());
        when(ms.getMessage(CART_ITEM_NOT_FOUND))
                .thenReturn(getMessage(CART_ITEM_NOT_FOUND));
        assertThatThrownBy(() -> cartService.deleteCartItemById(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CART_ITEM_NOT_FOUND));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 삭제할 권한이 없는 경우)")
    void deleteCartItemByIdTest_unit_noPermission(){
        Carts cart = new Carts(1L);
        CartItems cartItem = new CartItems(1L, 10);
        cart.addCartItem(cartItem);
        when(cartItemsRepository.findWithCartById(1L))
                .thenReturn(Optional.of(cartItem));
        when(ms.getMessage(CART_ITEM_NO_PERMISSION))
                .thenReturn(getMessage(CART_ITEM_NO_PERMISSION));

        assertThatThrownBy(() -> cartService.deleteCartItemById(99L, 1L))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage(getMessage(CART_ITEM_NO_PERMISSION));
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-성공")
    void clearAllCartItemsTest_unit_success(){
        Carts cart = new Carts(1L);
        cart.addCartItem(new CartItems(1L, 10));

        when(cartsRepository.findWithItemsByUserId(1L))
                .thenReturn(Optional.of(cart));

        cartService.clearAllCartItems(1L);

        assertThat(cart.getCartItems().size()).isEqualTo(0);
    }


}
