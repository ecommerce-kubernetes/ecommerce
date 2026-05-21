package com.example.order_service.ordersheet.application.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderSheetProductMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final OrderSheetProductMapper mapper = new OrderSheetProductMapperImpl(moneyMapper);

    @Test
    @DisplayName("상품 응답을 Result로 매핑한다")
    void toResult(){
        //given
        ProductClientResponse.ProductList response = getProductResponse();
        OrderSheetProductResult.ProductList expectedResult = getExpectedResult();
        //when
        OrderSheetProductResult.ProductList result = mapper.toResult(response);
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

    private OrderSheetProductResult.ProductList getExpectedResult() {
        OrderSheetProductResult.Option xl = OrderSheetProductResult.Option.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL")
                .build();
        OrderSheetProductResult.Option blue = OrderSheetProductResult.Option.builder()
                .optionTypeName("색상")
                .optionValueName("BLUE")
                .build();
        OrderSheetProductResult.Info product = OrderSheetProductResult.Info.builder()
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
        return OrderSheetProductResult.ProductList.builder()
                .products(List.of(product))
                .build();
    }
}
