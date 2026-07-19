package com.example.order_service.order.application.external.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
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
        OrderProductResult expectedResult = getExpectedResult();
        //when
        OrderProductResult result = mapper.toResult(response);
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

    private OrderProductResult getExpectedResult() {
        ProductOptionSnapshot xl = ProductOptionSnapshot.of("사이즈", "XL");
        ProductOptionSnapshot blue = ProductOptionSnapshot.of("색상", "BLUE");
        ProductSnapshot productSnapshot = ProductSnapshot.of(1L, 1L, "PROD1-XL-BLUE",
                "청바지", "/product/product/jean_1.jpg");
        ProductPriceSnapshot productPriceSnapshot = ProductPriceSnapshot.of(Money.wons(10000L), 10,
                Money.wons(1000L), Money.wons(9000L));
        OrderProductResult.OrderProductDetail product = OrderProductResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .status(OrderProductStatus.ON_SALE)
                .stock(100)
                .priceSnapshot(productPriceSnapshot)
                .options(List.of(xl, blue))
                .build();
        return OrderProductResult.builder()
                .products(List.of(product))
                .build();
    }
}
