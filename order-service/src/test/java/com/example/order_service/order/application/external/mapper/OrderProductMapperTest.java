package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderProductMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final OrderProductMapper mapper = new OrderProductMapperImpl(moneyMapper);

    @Test
    @DisplayName("상품 응답을 Result로 매핑한다")
    void toResult(){
        //given
        ProductClientResponse.ProductList response = getProductResponse();
        OrderProductResult.ProductList expectedResult = getExpectedResult();
        //when
        OrderProductResult.ProductList result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(expectedResult);
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

    private OrderProductResult.ProductList getExpectedResult() {
        OrderProductResult.Option xl = OrderProductResult.Option.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();
        OrderProductResult.Option blue = OrderProductResult.Option.builder()
                .optionTypeName("색상")
                .optionValueName("BLUE")
                .build();
        OrderProductResult.Info product = OrderProductResult.Info.builder()
                .productId(1L)
                .productVariantId(1L)
                .sku("PROD1-XL-BLUE")
                .productName("청바지")
                .originalPrice(Money.wons(10000L))
                .discountRate(10)
                .discountAmount(Money.wons(1000L))
                .discountedPrice(Money.wons(9000L))
                .thumbnail("/product/product/jean_1.jpg")
                .options(List.of(xl, blue))
                .build();
        return OrderProductResult.ProductList.builder()
                .products(List.of(product))
                .build();
    }
}
