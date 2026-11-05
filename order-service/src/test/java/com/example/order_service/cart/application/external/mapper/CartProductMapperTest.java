package com.example.order_service.cart.application.external.mapper;

import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartProductMapperTest {
    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final CartProductMapper mapper = new CartProductMapperImpl(moneyMapper);

    @Test
    @DisplayName("상품 응답을 Result로 매핑한다")
    void toResult() {
        //given
        ProductClientResponse.ProductList response = getProductResponse();
        CartProductListResult expectedResult = getExpectedResult();
        //when
        CartProductListResult result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    private CartProductListResult getExpectedResult() {
        CartProductResult.ProductOption xl = CartProductResult.ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();

        CartProductResult.ProductOption blue = CartProductResult.ProductOption.builder()
                .optionTypeName("색상")
                .optionValueName("BLUE")
                .build();

        CartProductResult result = CartProductResult.builder()
                .productId(1L)
                .productVariantId(1L)
                .status(CartProductStatus.ON_SALE)
                .stock(100)
                .sku("PROD1-XL-BLUE")
                .productName("청바지")
                .thumbnail("/product/product/jean_1.jpg")
                .originalPrice(Money.wons(10000L))
                .discountRate(10)
                .discountAmount(Money.wons(1000L))
                .discountedPrice(Money.wons(9000L))
                .options(List.of(xl, blue))
                .build();

        return CartProductListResult.builder()
                .products(List.of(result))
                .build();
    }

    private ProductClientResponse.ProductList getProductResponse() {
        ProductClientResponse.UnitPrice unitPrice = ProductClientResponse.UnitPrice.builder()
                .originalPrice(10000L)
                .discountRate(10)
                .discountAmount(1000L)
                .discountedPrice(9000L)
                .build();
        ProductClientResponse.ProductOption xl = ProductClientResponse.ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();
        ProductClientResponse.ProductOption blue = ProductClientResponse.ProductOption.builder()
                .optionTypeName("색상")
                .optionValueName("BLUE")
                .build();
        ProductClientResponse.Product product = ProductClientResponse.Product.builder()
                .productId(1L)
                .productVariantId(1L)
                .status("ON_SALE")
                .stock(100)
                .sku("PROD1-XL-BLUE")
                .productName("청바지")
                .thumbnail("/product/product/jean_1.jpg")
                .unitPrice(unitPrice)
                .options(List.of(xl, blue))
                .build();
        return ProductClientResponse.ProductList.builder()
                .products(List.of(product))
                .build();
    }
}