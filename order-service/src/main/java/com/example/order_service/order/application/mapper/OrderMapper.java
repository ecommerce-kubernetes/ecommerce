package com.example.order_service.order.application.mapper;

import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.application.service.order.dto.command.OrderContext;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderMapper {

    @Mapping(source = "items", target = "orderItems")
    OrderContext.CreateOrderContext toContext(OrderSheet orderSheet);
    OrderContext.ItemContext toItemContext(OrderSheetItem orderSheetItem);
}
