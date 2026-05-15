package com.example.order_service.order.application.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.dto.result.OrderProductResult;
import com.example.order_service.order.domain.model.vo.ProductStatus;
import com.example.order_service.support.TestFixtureUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderProductDeprecatedMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final OrderProductMapper mapper = new OrderProductMapperImpl(moneyMapper);

    @Test
    @DisplayName("상품 응답을 Result로 매핑한다")
    void toResult(){
        //given
        ProductClientResponse.ProductDeprecated response = TestFixtureUtil.sample(fixtureMonkey.giveMeBuilder(ProductClientResponse.ProductDeprecated.class)
                .set("status", "ON_SALE")
                .set("unitPrice.originalPrice", 10000L)
                .set("unitPrice.discountRate", 10)
                .set("unitPrice.discountAmount", 1000L)
                .set("unitPrice.discountedPrice", 9000L));

        List<OrderProductResult.Option> expectedOptions = response.itemOptions().stream()
                .map(opt -> OrderProductResult.Option.builder()
                        .optionTypeName(opt.optionTypeName())
                        .optionValueName(opt.optionValueName())
                        .build())
                .toList();

        OrderProductResult.Info expectedResult = OrderProductResult.Info.builder()
                .productId(response.productId())
                .productVariantId(response.productVariantId())
                .status(ProductStatus.ORDERABLE)
                .sku(response.sku())
                .productName(response.productName())
                .thumbnail(response.thumbnail())
                .originalPrice(Money.wons(response.unitPrice().originalPrice()))
                .discountRate(response.unitPrice().discountRate())
                .discountAmount(Money.wons(response.unitPrice().discountAmount()))
                .discountedPrice(Money.wons(response.unitPrice().discountedPrice()))
                .stock(response.stockQuantity())
                .options(expectedOptions)
                .build();
        //when
        OrderProductResult.Info result = mapper.toResult(response);
        //then
        assertThat(result)
                .usingRecursiveComparison()
                .withEqualsForType(Money::equals, Money.class)
                .isEqualTo(expectedResult);
    }
}
