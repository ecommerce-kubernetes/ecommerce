package com.example.order_service.cart.application.external.mapper;


import com.example.order_service.common.mapper.MoneyMapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CartProductMapper {




}
