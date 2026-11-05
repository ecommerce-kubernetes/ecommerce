package com.example.order_service.order.application.external.mapper;


import com.example.order_service.common.mapper.MoneyMapper;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MoneyMapper.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface OrderProductMapper {

    OrderProductResult.ProductList toResult(ProductClientResponse.ProductList productList);

    @Mapping(source = ".", target = "productSnapshot")
    @Mapping(source = "unitPrice", target = "priceSnapshot")
    @Mapping(source = "options", target = "options")
    OrderProductResult.Info toProduct(ProductClientResponse.Product product);

    ProductSnapshot toProductSnapshot(ProductClientResponse.Product product);

    ProductPriceSnapshot toPriceSnapshot(ProductClientResponse.UnitPrice unitPrice);

    ProductOptionSnapshot toOptionSnapshot(ProductClientResponse.ProductOption option);

    default OrderProductStatus toOrderStatus(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case "ON_SALE" -> OrderProductStatus.ON_SALE;
            case "PREPARING" -> OrderProductStatus.PREPARING;
            case "STOP_SALE" -> OrderProductStatus.STOP_SALE;
            case "DELETED" -> OrderProductStatus.DELETED;
            default -> OrderProductStatus.UNKNOWN;
        };
    }
}
