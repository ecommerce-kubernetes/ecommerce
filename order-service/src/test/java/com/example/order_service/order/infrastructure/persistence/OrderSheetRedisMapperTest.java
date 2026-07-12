package com.example.order_service.order.infrastructure.persistence;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.order.domain.model.OrderSheet;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.all;

public class OrderSheetRedisMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
    private final OrderSheetRedisMapper mapper = new OrderSheetRedisMapperImpl(moneyMapper);

    @Test
    @DisplayName("도메인, 엔티티 데이터를 양방향 매핑한다")
    void mapperRoundTripTest(){
        //given
        OrderSheet orderSheet = Instancio.of(OrderSheet.class)
                .supply(all(Money.class), () -> {
                    long amount = Instancio.gen().longs().range(1000L, 100000L).get();
                    return Money.wons(amount);
                })
                .create();
        //when
        OrderSheetRedisEntity entity = mapper.toEntity(orderSheet);
        OrderSheet domain = mapper.toDomain(entity);
        //then
        assertThat(domain)
                .usingRecursiveComparison()
                .isEqualTo(orderSheet);
    }
}
