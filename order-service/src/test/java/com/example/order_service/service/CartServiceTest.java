package com.example.order_service.service;

import com.example.order_service.dto.request.CartItemRequest;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.CartResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.ProductInfo;
import com.example.order_service.entity.CartItems;
import com.example.order_service.entity.Carts;
import com.example.order_service.exception.NoPermissionException;
import com.example.order_service.exception.NotFoundException;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.client.dto.ProductResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.order_service.common.MessagePath.*;
import static com.example.order_service.util.TestMessageUtil.getMessage;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class CartServiceTest {

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry){
        registry.add("product-service.url", () -> "http://localhost:${wiremock.server.port}");
    }

    @Autowired
    CartsRepository cartsRepository;
    @Autowired
    EntityManager em;
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    CartService cartService;
    Carts cart;
    CartItems cartItem;
    @BeforeEach
    void setFixture(){
        cart = new Carts(1L);
        cartItem = new CartItems(1L, 10);
        cart.addCartItem(cartItem);
        cartsRepository.save(cart);
    }

    @Test
    @DisplayName("장바구니 상품 추가 테스트-성공(처음 상품 추가시)")
    @Transactional
    void addItemTest_integration_success_newCart() throws JsonProcessingException {
        Long newUserId = 2L;
        Long productVariantId = 100L;
        int quantity = 5;
        CartItemRequest request = new CartItemRequest(productVariantId, quantity);
        ProductResponse productResponse = new ProductResponse(1L, productVariantId, "상품1", 3000, 10, "http://product1.jpg",
                List.of(new ItemOptionResponse("색상", "RED")));

        stubFor(get(urlEqualTo("/variants/" + productVariantId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(productResponse))));

        CartItemResponse response = cartService.addItem(newUserId, request);
        em.flush();
        em.clear();

        assertThat(response.getId()).isNotNull();
        assertThat(response)
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(5, true);

        assertThat(response.getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 100L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getProductInfo().getItemOptions())
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(tuple("색상", "RED"));
    }

    @Test
    @DisplayName("장바구니 상품 추가 테스트-성공(새로운 상품 추가시)")
    @Transactional
    void addItemTest_integration_success_newItem() throws JsonProcessingException {
        Long productVariantId = 100L;
        int quantity = 5;
        CartItemRequest request = new CartItemRequest(productVariantId, quantity);
        ProductResponse productResponse = new ProductResponse(1L, productVariantId, "상품1", 3000, 10, "http://product1.jpg",
                List.of(new ItemOptionResponse("색상", "RED")));

        stubFor(get(urlEqualTo("/variants/" + productVariantId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(productResponse))));

        CartItemResponse response = cartService.addItem(1L, request);
        em.flush();
        em.clear();

        assertThat(response.getId()).isNotNull();
        assertThat(response)
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(5, true);

        assertThat(response.getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 100L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getProductInfo().getItemOptions())
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(tuple("색상", "RED"));
    }

    @Test
    @DisplayName("장바구니 상품 추가 테스트-성공(장바구니에 이미 존재하는 상품 추가시)")
    @Transactional
    void addItemTest_integration_success_existItem() throws JsonProcessingException {
        Long productVariantId = cartItem.getProductVariantId();
        int quantity = 5;
        CartItemRequest request = new CartItemRequest(productVariantId, quantity);
        ProductResponse productResponse = new ProductResponse(1L, productVariantId, "상품1", 3000, 10, "http://product1.jpg",
                List.of(new ItemOptionResponse("색상", "RED")));

        stubFor(get(urlEqualTo("/variants/" + productVariantId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(mapper.writeValueAsString(productResponse))));

        CartItemResponse response = cartService.addItem(1L, request);
        em.flush();
        em.clear();

        assertThat(response.getId()).isNotNull();
        assertThat(response)
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(15, true);

        assertThat(response.getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, cartItem.getProductVariantId(), "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getProductInfo().getItemOptions())
                .extracting(ItemOptionResponse::getOptionTypeName, ItemOptionResponse::getOptionValueName)
                .containsExactlyInAnyOrder(tuple("색상", "RED"));
    }

    @Test
    @DisplayName("장바구니 상품 추가 테스트-실패(상품을 찾을 수 없음)")
    @Transactional
    void addItemTest_integration_notFound_product(){
        Long productVariantId = 999L;
        int quantity = 5;
        CartItemRequest request = new CartItemRequest(productVariantId, quantity);
        stubFor(get(urlEqualTo("/variants/" + productVariantId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")));

        assertThatThrownBy(() -> cartService.addItem(1L, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(PRODUCT_VARIANT_NOT_FOUND));

    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-성공(장바구니가 없는 경우)")
    @Transactional
    void getCartItemListTest_integration_noCart(){
        CartResponse response = cartService.getCartItemList(2L);
        assertThat(response.getCartTotalPrice()).isEqualTo(0);
        assertThat(response.getCartItems()).isEmpty();
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-성공(장바구니에 상품이 없는 경우)")
    @Transactional
    void getCartItemListTest_integration_noItems(){
        cartsRepository.save(new Carts(2L));
        em.flush(); em.clear();

        CartResponse response = cartService.getCartItemList(2L);
        assertThat(response.getCartItems()).isEmpty();
        assertThat(response.getCartTotalPrice()).isEqualTo(0);
    }

    @Test
    @DisplayName("장바구니 목록 조회 테스트-성공(장바구니 상품 존재)")
    @Transactional
    void getCartItemListTest_integration_existItem() throws JsonProcessingException {
        Carts cart = new Carts(2L);
        CartItems cartItem1 = new CartItems(1L, 2);
        CartItems cartItem2 = new CartItems(2L, 5);
        cart.addCartItem(cartItem1);
        cart.addCartItem(cartItem2);
        cartsRepository.save(cart);
        em.flush(); em.clear();
        String productRequest = mapper.writeValueAsString(List.of(1L, 2L));
        String productResponse = mapper.writeValueAsString(List.of(new ProductResponse(1L, 1L, "상품1", 3000, 10, "http://product1.jpg",
                        List.of(new ItemOptionResponse("색상", "RED"))),
                new ProductResponse(2L, 2L, "상품2", 5000, 5, "http://product2.jpg",
                        List.of(new ItemOptionResponse("사이즈", "XL")))));
        stubFor(post(urlEqualTo("/variants/by-ids"))
                .withRequestBody(equalToJson(productRequest, true, true))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(productResponse)));


        CartResponse response = cartService.getCartItemList(2L);
        assertThat(response.getCartTotalPrice()).isEqualTo(29150);
        assertThat(response.getCartItems())
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(
                        tuple(2, true),
                        tuple(5, true)
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
    @DisplayName("장바구니 목록 조회 테스트-성공(장바구니에 삭제된 상품 존재)")
    @Transactional
    void getCartItemListTest_integration_deletedItems() throws JsonProcessingException{
        Carts cart = new Carts(2L);
        CartItems cartItem1 = new CartItems(1L, 2);
        CartItems cartItem2 = new CartItems(2L, 5);
        cart.addCartItem(cartItem1);
        cart.addCartItem(cartItem2);
        cartsRepository.save(cart);
        em.flush(); em.clear();

        String productRequest = mapper.writeValueAsString(List.of(1L, 2L));
        String productResponse = mapper.writeValueAsString(List.of(new ProductResponse(1L, 1L, "상품1", 3000, 10, "http://product1.jpg",
                        List.of(new ItemOptionResponse("색상", "RED")))));
        stubFor(post(urlEqualTo("/variants/by-ids"))
                .withRequestBody(equalToJson(productRequest, true, true))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(productResponse)));

        CartResponse response = cartService.getCartItemList(2L);
        assertThat(response.getCartTotalPrice()).isEqualTo(5400);
        assertThat(response.getCartItems())
                .extracting(CartItemResponse::getQuantity, CartItemResponse::isAvailable)
                .containsExactlyInAnyOrder(
                        tuple(2, true),
                        tuple(5, false)
                );

        assertThat(response.getCartItems().get(0).getProductInfo())
                .extracting(ProductInfo::getProductId, ProductInfo::getProductVariantId, ProductInfo::getProductName,
                        ProductInfo::getPrice, ProductInfo::getDiscountRate, ProductInfo::getThumbnailUrl)
                .containsExactlyInAnyOrder(
                        1L, 1L, "상품1", 3000, 10, "http://product1.jpg"
                );

        assertThat(response.getCartItems().get(1).getProductInfo()).isNull();
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-성공")
    @Transactional
    void deleteCartItemByIdTest_integration_success(){
        cartService.deleteCartItemById(1L, cartItem.getId());
        em.flush(); em.clear();

        Carts cart = cartsRepository.findWithItemsByUserId(1L).get();

        assertThat(cart.getCartItems()).hasSize(0);
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(장바구니 상품을 찾을 수 없는 경우)")
    @Transactional
    void deleteCartItemByIdTest_integration_notFound_cartItem(){
        assertThatThrownBy(() -> cartService.deleteCartItemById(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(getMessage(CART_ITEM_NOT_FOUND));
    }

    @Test
    @DisplayName("장바구니 상품 삭제 테스트-실패(삭제할 권한이 없는 경우)")
    @Transactional
    void deleteCartItemByIdTest_integration_noPermission(){
        assertThatThrownBy(() -> cartService.deleteCartItemById(99L, cartItem.getId()))
                .isInstanceOf(NoPermissionException.class)
                .hasMessage(getMessage(CART_ITEM_NO_PERMISSION));
    }

    @Test
    @DisplayName("장바구니 비우기 테스트-성공")
    @Transactional
    void clearAllCartItemTest_integration_success(){
        cartService.clearAllCartItems(1L);
        em.flush(); em.clear();

        Carts findCart = cartsRepository.findWithItemsByUserId(1L).get();

        assertThat(findCart.getCartItems()).hasSize(0);
    }
}