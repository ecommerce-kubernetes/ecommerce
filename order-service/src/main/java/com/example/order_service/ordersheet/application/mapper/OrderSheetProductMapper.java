package com.example.order_service.ordersheet.application.mapper;


import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.ordersheet.application.dto.result.OrderSheetProductResult;
import com.example.order_service.ordersheet.domain.model.vo.ProductStatus;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderSheetProductMapper {

    @Mapping(source = "stockQuantity", target = "stock")
    @Mapping(source = "unitPrice.originalPrice", target = "originalPrice")
    @Mapping(source = "unitPrice.discountRate", target = "discountRate")
    @Mapping(source = "unitPrice.discountAmount", target = "discountAmount")
    @Mapping(source = "unitPrice.discountedPrice", target = "discountedPrice")
    @Mapping(source = "itemOptions", target = "options")
    OrderSheetProductResult.Info toResult(ProductClientResponse.ProductDeprecated productDeprecated);

    OrderSheetProductResult.Option toOption(ProductClientResponse.ProductOption option);

    default ProductStatus translateStatus(String status) {
        if (status == null) {
            return ProductStatus.UNORDERABLE;
        }

        if (status.equals("ON_SALE")) {
            return ProductStatus.ORDERABLE;
        } else {
            return ProductStatus.UNORDERABLE;
        }
    }
}
