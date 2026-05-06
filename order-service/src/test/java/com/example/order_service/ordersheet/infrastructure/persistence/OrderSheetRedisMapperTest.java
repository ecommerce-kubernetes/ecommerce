package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.common.mapper.MoneyMapper;
import org.mapstruct.factory.Mappers;

public class OrderSheetRedisMapperTest {

    private final MoneyMapper moneyMapper = Mappers.getMapper(MoneyMapper.class);
}
