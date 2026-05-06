package com.example.order_service.cart.application.mapper;

import com.example.order_service.cart.application.dto.result.CartProductResult;
import com.example.order_service.cart.domain.model.vo.ProductStatus;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.support.TestFixtureUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;

public class CartProductMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final CartProductMapper mapper = new CartProductMapperImpl(moneyMapper);

    @Test
    @DisplayName("상품 응답을 Result로 매핑한다")
    void toResult() {
        //given
        // source 데이터
        ProductClientResponse.Product source = TestFixtureUtil.sample(fixtureMonkey.giveMeBuilder(ProductClientResponse.Product.class)
                .set("status", "ON_SALE")
                .set("unitPrice.originalPrice", 10000L)
                .set("unitPrice.discountRate", 10)
                .set("unitPrice.discountAmount", 1000L)
                .set("unitPrice.discountedPrice", 9000L));
        // 변환된 result
        List<CartProductResult.Option> expectedOptions = source.itemOptions().stream()
                .map(opt -> CartProductResult.Option.builder()
                        .optionTypeName(opt.optionTypeName())
                        .optionValueName(opt.optionValueName())
                        .build())
                .toList();
        CartProductResult.Info expectedResult = CartProductResult.Info.builder()
                .productId(source.productId())
                .productVariantId(source.productVariantId())
                .status(ProductStatus.AVAILABLE)
                .sku(source.sku())
                .productName(source.productName())
                .thumbnail(source.thumbnail())
                .originalPrice(Money.wons(source.unitPrice().originalPrice()))
                .discountRate(source.unitPrice().discountRate())
                .discountAmount(Money.wons(source.unitPrice().discountAmount()))
                .discountedPrice(Money.wons(source.unitPrice().discountedPrice()))
                .stock(source.stockQuantity())
                .options(expectedOptions)
                .build();
        //when
        CartProductResult.Info result = mapper.toResult(source);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .withEqualsForType(Money::equals, Money.class)
                .isEqualTo(expectedResult);
    }
}
