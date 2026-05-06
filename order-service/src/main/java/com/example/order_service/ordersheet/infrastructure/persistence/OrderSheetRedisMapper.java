package com.example.order_service.ordersheet.infrastructure.persistence;

import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.ordersheet.domain.model.OrderSheet;
import com.example.order_service.ordersheet.domain.model.OrderSheetItem;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
interface OrderSheetRedisMapper {
    OrderSheetRedisEntity toEntity(OrderSheet domain);
    OrderSheet toDomain(OrderSheetRedisEntity entity);

    @Mapping(source = "itemPrice", target = "priceSnapshot")
    OrderSheetRedisEntity.OrderSheetItemRedisEntity toItemEntity(OrderSheetItem domain);
    @Mapping(source = "priceSnapshot", target = "itemPrice")
    OrderSheetItem toItemDomain(OrderSheetRedisEntity.OrderSheetItemRedisEntity entity);


}
