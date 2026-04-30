package com.example.order_service.ordersheet.application.mapper;


import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.application.dto.result.ProductStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = ProductStatus.class)
public interface OrderSheetProductMapper {

    @Mapping(source = "stockQuantity", target = "stock")
    @Mapping(source = "unitPrice.originalPrice", target = "originalPrice")
    @Mapping(source = "unitPrice.discountRate", target = "discountRate")
    @Mapping(source = "unitPrice.discountAmount", target = "discountAmount")
    @Mapping(source = "unitPrice.discountedPrice", target = "discountedPrice")
    @Mapping(target = "status", expression = "java(ProductStatus.from(product.status()))")
    OrderSheetProductResult.Info toResult(ProductClientResponse.Product product);

    OrderSheetProductResult.Option toOption(ProductClientResponse.ProductOption option);
}
