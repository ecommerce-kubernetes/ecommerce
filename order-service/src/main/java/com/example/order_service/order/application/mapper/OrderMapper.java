package com.example.order_service.order.application.mapper;

import com.example.order_service.order.application.dto.result.OrderResult;
import com.example.order_service.order.domain.model.OrderSheet;
import com.example.order_service.order.domain.model.OrderSheetItem;
import com.example.order_service.order.domain.service.dto.command.OrderContext;
import com.example.order_service.order.domain.service.dto.result.OrderDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderMapper {

    @Mapping(source = "items", target = "orderItems")
    OrderContext.CreateOrderContext toContext(OrderSheet orderSheet);
    OrderContext.ItemContext toItemContext(OrderSheetItem orderSheetItem);
    OrderResult.Create toResult(OrderDto orderDto);

}
