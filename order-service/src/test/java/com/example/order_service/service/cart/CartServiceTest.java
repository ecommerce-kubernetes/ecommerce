package com.example.order_service.service.cart;

import com.example.order_service.common.MessageSourceUtil;
import com.example.order_service.common.security.UserPrincipal;
import com.example.order_service.common.security.UserRole;
import com.example.order_service.dto.response.CartItemResponse;
import com.example.order_service.dto.response.ItemOptionResponse;
import com.example.order_service.dto.response.UnitPrice;
import com.example.order_service.entity.Carts;
import com.example.order_service.repository.CartsRepository;
import com.example.order_service.service.CartService;
import com.example.order_service.service.ExcludeInfraIntegrationTestSupport;
import com.example.order_service.service.client.ProductClientService;
import com.example.order_service.service.client.dto.ProductResponse;
import com.example.order_service.service.dto.AddCartItemDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

class CartServiceTest extends ExcludeInfraIntegrationTestSupport {
    @MockitoBean
    private ProductClientService productClientService;
    @MockitoBean
    private MessageSourceUtil messageSourceUtil;
    @Autowired
    private CartService cartService;
    @Autowired
    private CartsRepository cartsRepository;

    @Test
    @DisplayName("처음 장바구니에 상품을 추가하면 장바구니를 생성하고 상품을 추가한다")
    @Transactional
    void addItem_firstAdd(){
        //given
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