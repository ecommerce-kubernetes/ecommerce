package com.example.order_service.ordersheet.application.mapper;


import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderSheetProductMapper {

    OrderSheetProductResult.ProductList toResult(ProductClientResponse.ProductList productList);
    @Mapping(source = "unitPrice.originalPrice", target = "originalPrice")
    @Mapping(source = "unitPrice.discountRate", target = "discountRate")
    @Mapping(source = "unitPrice.discountAmount", target = "discountAmount")
    @Mapping(source = "unitPrice.discountedPrice", target = "discountedPrice")
    OrderSheetProductResult.Info toProduct(ProductClientResponse.Product product);
    OrderSheetProductResult.Option toOption(ProductClientResponse.ProductOption option);
}
