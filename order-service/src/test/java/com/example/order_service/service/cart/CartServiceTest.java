package com.example.order_service.service.cart;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.common.security.UserRole;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.exception.server.InternalServerException;
import com.example.order_service.exception.server.UnavailableServerException;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.CartService;
import com.example.order_service.service.ExcludeInfraIntegrationTestSupport;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.service.dto.AddCartItemDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

class CartServiceTest extends ExcludeInfraIntegrationTestSupport {
    @MockitoBean
    private ProductClientService productClientService;
    @MockitoBean
    private MessageSourceUtil messageSourceUtil;
    @Autowired
    private CartService cartService;
    @Autowired
    private CartsRepository cartsRepository;
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("처음 장바구니에 상품을 추가하면 장바구니를 생성하고 상품을 추가한다")
    @Transactional
    void addItem_firstAdd(){
        //given
        AddCartItemDto dto = createAddCartItemDto(1L, 3);
        ProductResponse productResponse = createProductResponse(1L, 1L, "상품1",
                3000L, 10, "http://thumbnail.jpg",
                List.of(ItemOptionResponse.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL")
                        .build()));
        given(productClientService.fetchProductByVariantId(anyLong()))
                .willReturn(productResponse);

        //when
        CartItemResponse response = cartService.addItem(dto);
        //then
        assertThat(response.getId()).isNotNull();
        assertThat(response)
                .extracting(
                        "productId",
                        "productName",
                        "thumbnailUrl",
                        "quantity",
                        "lineTotal",
                        "isAvailable"
                )
                .contains(
                        1L, "상품1", "http://thumbnail.jpg", 3, 8100L, true
                );

        assertThat(response.getUnitPrice())
                .extracting(
                        "originalPrice",
                        "discountRate",
                        "discountAmount",
                        "discountedPrice"
                )
                .contains(
                        3000L,
                        10,
                        300L,
                        2700L
                );

        assertThat(response.getOptions())
                .extracting(
                        "optionTypeName",
                        "optionValueName"
                )
                .containsExactlyInAnyOrder(
                        tuple("사이즈", "XL")
                );

        Optional<Carts> cart = cartsRepository.findByUserId(1L);
        assertThat(cart).isNotNull();
    }

    @Test
    @DisplayName("처음 이후 장바구니에 새로운 상품을 추가하면 기존 장바구니에 상품을 추가한다")
    void addItem_subsequent(){
        //given
        AddCartItemDto dto = createAddCartItemDto(1L, 3);
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        cartsRepository.save(cart);
        ProductResponse productResponse = createProductResponse(1L, 1L, "상품1", 3000L, 10, "http://thumbnail.jpg",
                List.of(ItemOptionResponse.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL")
                        .build()));
        given(productClientService.fetchProductByVariantId(anyLong()))
                .willReturn(productResponse);
        //when
        CartItemResponse response = cartService.addItem(dto);
        //then
        assertThat(response.getId()).isNotNull();
        assertThat(response)
                .extracting(
                        "productId",
                        "productName",
                        "thumbnailUrl",
                        "quantity",
                        "lineTotal",
                        "isAvailable"
                )
                .contains(
                        1L, "상품1", "http://thumbnail.jpg", 3, 8100L, true
                );

        assertThat(response.getUnitPrice())
                .extracting(
                        "originalPrice",
                        "discountRate",
                        "discountAmount",
                        "discountedPrice"
                )
                .contains(
                        3000L,
                        10,
                        300L,
                        2700L
                );

        assertThat(response.getOptions())
                .extracting(
                        "optionTypeName",
                        "optionValueName"
                )
                .containsExactlyInAnyOrder(
                        tuple("사이즈", "XL")
                );
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 추가하려는 상품이 이미 장바구니에 존재하는 상품이면 수량을 요청 수량만큼 증가시킨다")
    void addItem_existProduct() {
        //given
        Carts cart = Carts.builder()
                .userId(1L)
                .build();
        CartItems cartItem = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        cart.addCartItem(cartItem);
        cartsRepository.save(cart);

        AddCartItemDto dto = createAddCartItemDto(1L, 3);
        ProductResponse productResponse = createProductResponse(1L, 1L, "상품1", 3000L, 10, "http://thumbnail.jpg",
                List.of(ItemOptionResponse.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL")
                        .build()));
        given(productClientService.fetchProductByVariantId(anyLong()))
                .willReturn(productResponse);
        //when
        CartItemResponse response = cartService.addItem(dto);
        //then
        assertThat(response.getId()).isNotNull();
        assertThat(response)
                .extracting(
                        "productId",
                        "productName",
                        "thumbnailUrl",
                        "quantity",
                        "lineTotal",
                        "isAvailable"
                )
                .contains(
                        1L, "상품1", "http://thumbnail.jpg", 6, 16200L, true
                );

        assertThat(response.getUnitPrice())
                .extracting(
                        "originalPrice",
                        "discountRate",
                        "discountAmount",
                        "discountedPrice"
                )
                .contains(
                        3000L,
                        10,
                        300L,
                        2700L
                );

        assertThat(response.getOptions())
                .extracting(
                        "optionTypeName",
                        "optionValueName"
                )
                .containsExactlyInAnyOrder(
                        tuple("사이즈", "XL")
                );
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 상품 정보를 찾을 수 없으면 NotFoundException를 반환한다")
    void addItemWhenNotFoundProduct() {
        //given
        AddCartItemDto dto = createAddCartItemDto(1L, 3);
        willThrow(new NotFoundException("해당 상품을 찾을 수 없습니다"))
                .given(productClientService).fetchProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartService.addItem(dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("해당 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 상품 서비스에서 503에러가 발생하면 UnavailableServerException를 반환한다")
    void addItemWhen503Error() {
        //given
        AddCartItemDto dto = createAddCartItemDto(1L, 3);
        willThrow(new UnavailableServerException("상품을 불러올 수 없습니다 잠시후 다시 시도해주세요"))
                .given(productClientService).fetchProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartService.addItem(dto))
                .isInstanceOf(UnavailableServerException.class)
                .hasMessage("상품을 불러올 수 없습니다 잠시후 다시 시도해주세요");
    }

    @Test
    @DisplayName("장바구니에 상품을 추가할때 상품 서비스에서 500에러가 발생한 경우 InternalServerException을 반환한다")
    void addItemWhen500Error() {
        //given
        AddCartItemDto dto = createAddCartItemDto(1L, 3);
        willThrow(new InternalServerException("상품을 불러올 수 없습니다"))
                .given(productClientService).fetchProductByVariantId(anyLong());
        //when
        //then
        assertThatThrownBy(() -> cartService.addItem(dto))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("상품을 불러올 수 없습니다");
    }

    @Test
    @DisplayName("장바구니에 추가된 상품의 정보 목록을 조회한다")
    void getCartItemList() {
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();
        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();
        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);

        ProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail1.jpg", List.of(ItemOptionResponse.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL").build()));
        ProductResponse product2 = createProductResponse(2L, 2L, "상품2", 5000L, 10,
                "http://thumbnail2.jpg", List.of(ItemOptionResponse.builder()
                        .optionTypeName("용량")
                        .optionValueName("256GB").build()));
        given(productClientService.fetchProductByVariantIds(anyList()))
                .willReturn(List.of(product1, product2));
        //when
        CartResponse response = cartService.getCartItemList(userPrincipal);
        //then
        assertThat(response.getCartItems())
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        itemResponse1 -> {
                            assertThat(itemResponse1.getId()).isNotNull();
                            assertThat(itemResponse1.getProductId()).isEqualTo(1L);
                            assertThat(itemResponse1.getProductName()).isEqualTo("상품1");
                            assertThat(itemResponse1.getThumbnailUrl()).isEqualTo("http://thumbnail1.jpg");
                            assertThat(itemResponse1.getQuantity()).isEqualTo(3);
                            assertThat(itemResponse1.getLineTotal()).isEqualTo(8100);
                            assertThat(itemResponse1.isAvailable()).isTrue();
                            assertThat(itemResponse1.getUnitPrice())
                                    .isNotNull()
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .containsExactly(3000L, 10, 300L, 2700L);
                            assertThat(itemResponse1.getOptions())
                                    .hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactly(tuple("사이즈", "XL"));
                        },
                        itemResponse2 -> {
                            assertThat(itemResponse2.getId()).isNotNull();
                            assertThat(itemResponse2.getProductId()).isEqualTo(2L);
                            assertThat(itemResponse2.getProductName()).isEqualTo("상품2");
                            assertThat(itemResponse2.getThumbnailUrl()).isEqualTo("http://thumbnail2.jpg");
                            assertThat(itemResponse2.getQuantity()).isEqualTo(2);
                            assertThat(itemResponse2.getLineTotal()).isEqualTo(9000);
                            assertThat(itemResponse2.isAvailable()).isTrue();
                            assertThat(itemResponse2.getUnitPrice())
                                    .isNotNull()
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .containsExactly(5000L, 10, 500L, 4500L);
                            assertThat(itemResponse2.getOptions())
                                    .hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactly(tuple("용량", "256GB"));
                        }
                );
        assertThat(response.getCartTotalPrice()).isEqualTo(17100);
    }

    @Test
    @DisplayName("장바구니에 담긴 상품의 목록을 불러올때 특정 상품을 찾을 수 없는 상품이 존재하는 경우 해당 상품의 정보는 찾을 수 없음으로 반환한다")
    void getCartItemListWhenNotFoundProduct(){
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();
        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();
        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);

        ProductResponse product1 = createProductResponse(1L, 1L, "상품1", 3000L, 10,
                "http://thumbnail1.jpg", List.of(ItemOptionResponse.builder()
                        .optionTypeName("사이즈")
                        .optionValueName("XL").build()));

        given(productClientService.fetchProductByVariantIds(anyList()))
                .willReturn(List.of(product1));
        //when
        CartResponse response = cartService.getCartItemList(userPrincipal);
        //then
        assertThat(response.getCartItems())
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        itemResponse1 -> {
                            assertThat(itemResponse1.getId()).isNotNull();
                            assertThat(itemResponse1.getProductId()).isEqualTo(1L);
                            assertThat(itemResponse1.getProductName()).isEqualTo("상품1");
                            assertThat(itemResponse1.getThumbnailUrl()).isEqualTo("http://thumbnail1.jpg");
                            assertThat(itemResponse1.getQuantity()).isEqualTo(3);
                            assertThat(itemResponse1.getLineTotal()).isEqualTo(8100);
                            assertThat(itemResponse1.isAvailable()).isTrue();
                            assertThat(itemResponse1.getUnitPrice())
                                    .isNotNull()
                                    .extracting("originalPrice", "discountRate", "discountAmount", "discountedPrice")
                                    .containsExactly(3000L, 10, 300L, 2700L);
                            assertThat(itemResponse1.getOptions())
                                    .hasSize(1)
                                    .extracting("optionTypeName", "optionValueName")
                                    .containsExactly(tuple("사이즈", "XL"));
                        },
                        itemResponse2 -> {
                            assertThat(itemResponse2.getId()).isNotNull();
                            assertThat(itemResponse2.getProductId()).isNull();
                            assertThat(itemResponse2.getProductName()).isEqualTo("정보를 불러올 수 없거나 판매 중지된 상품입니다");
                            assertThat(itemResponse2.getThumbnailUrl()).isNull();
                            assertThat(itemResponse2.getQuantity()).isEqualTo(2);
                            assertThat(itemResponse2.getLineTotal()).isEqualTo(0);
                            assertThat(itemResponse2.isAvailable()).isFalse();
                            assertThat(itemResponse2.getUnitPrice()).isNull();
                            assertThat(itemResponse2.getOptions()).isNull();
                        }
                );
        assertThat(response.getCartTotalPrice()).isEqualTo(8100);
    }

    @Test
    @DisplayName("장바구니의 상품을 제거한다")
    void deleteCartItemById(){
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();

        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();

        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);
        //when
        cartService.deleteCartItemById(userPrincipal, 1L);
        //then
        Optional<Carts> findCart = cartsRepository.findWithItemsByUserId(1L);
        assertThat(findCart).isNotEmpty();
        assertThat(findCart.get().getCartItems()).hasSize(1);
        assertThat(findCart.get().getCartItems())
                .extracting("productVariantId", "quantity")
                .contains(
                        tuple(2L, 2)
                );
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 삭제할때 상품을 찾을 수 없는 경우 NotFoundException을 반환한다")
    void deleteCartItemByIdWhenNotFoundItem(){
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();

        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        cart.addCartItem(item1);
        cartsRepository.save(cart);
        //when
        //then
        assertThatThrownBy(() -> cartService.deleteCartItemById(userPrincipal, 999L))
                .isInstanceOf(NotFoundException.class)
                        .hasMessage("장바구니에 해당 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("장바구니에 상품을 삭제할때 장바구니 userId가 동일하지 않는 경우 NoPermissionException을 반환한다")
    void deleteCartItemByIdWhenNotMatchCartUserId(){
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(2L)
                .userRole(UserRole.ROLE_USER)
                .build();

        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();

        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();

        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);
        //when
        //then
        assertThatThrownBy(() -> cartService.deleteCartItemById(userPrincipal, item1.getId()))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage("장바구니의 상품을 삭제할 권한이 없습니다");
    }

    @Test
    @DisplayName("장바구니에 담긴 상품을 모두 삭제한다")
    void clearCart() {
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();

        Carts cart = Carts.builder()
                .userId(1L)
                .build();

        CartItems item1 = CartItems.builder()
                .productVariantId(1L)
                .quantity(3)
                .build();
        CartItems item2 = CartItems.builder()
                .productVariantId(2L)
                .quantity(2)
                .build();

        cart.addCartItem(item1);
        cart.addCartItem(item2);
        cartsRepository.save(cart);
        //when
        cartService.clearAllCartItems(userPrincipal);
        //then
        Optional<Carts> findCart = cartsRepository.findWithItemsByUserId(1L);
        assertThat(findCart).isNotEmpty();
        assertThat(findCart.get().getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("장바구니 상품을 모두 삭제할때 유저의 장바구니가 없는 경우 404 예외를 반환한다")
    void clearCartWhenNotFoundUserCart() {
        //given
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .userId(1L)
                .userRole(UserRole.ROLE_USER)
                .build();
        //when
        //then
        assertThatThrownBy(() -> cartService.clearAllCartItems(userPrincipal))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("장바구니를 찾을 수 없습니다");
    }

    private AddCartItemDto createAddCartItemDto(Long productVariantId, int quantity){
        return AddCartItemDto.builder()
                .userPrincipal(UserPrincipal.builder().userId(1L).userRole(UserRole.ROLE_USER).build())
                .productVariantId(productVariantId)
                .quantity(quantity)
                .build();
    }

    private ProductResponse createProductResponse(Long productId, Long productVariantId,
                                                  String productName, Long originalPrice, int discountRate,
                                                  String thumbnail, List<ItemOptionResponse> options){
        long discountAmount = originalPrice * discountRate / 100;
        return ProductResponse.builder()
                .productId(productId)
                .productVariantId(productVariantId)
                .productName(productName)
                .unitPrice(
                        UnitPrice.builder()
                                .originalPrice(originalPrice)
                                .discountRate(discountRate)
                                .discountAmount(discountAmount)
                                .discountedPrice(originalPrice - discountAmount)
                                .build())
                .thumbnailUrl(thumbnail)
                .itemOptions(options)
                .build();
    }
}