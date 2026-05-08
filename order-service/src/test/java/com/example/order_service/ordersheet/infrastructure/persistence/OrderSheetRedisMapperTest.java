package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static com.example.order_service.support.TestFixtureUtil.fixtureMonkey;
import static org.assertj.core.api.Assertions.assertThat;

public class OrderSheetRedisMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final OrderSheetRedisMapper mapper = new OrderSheetRedisMapperImpl(moneyMapper);

    @Test
    @DisplayName("도메인, 엔티티 데이터를 양방향 매핑한다")
    void mapperRoundTripTest(){
        //given
        OrderSheet orderSheet = fixtureMonkey.giveMeBuilder(OrderSheet.class)
                .size("items", 1, 3)
                .set("items[*].quantity", Arbitraries.integers().greaterOrEqual(1))
                .sample();

        //when
        OrderSheetRedisEntity entity = mapper.toEntity(orderSheet);
        OrderSheet domain = mapper.toDomain(entity);
        //then
        assertThat(domain)
                .usingRecursiveComparison()
                .isEqualTo(orderSheet);
    }
}
