package com.example.order_service.order.infrastructure.adaptor.mapper;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.DefaultPortException;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.order.application.port.dto.OrderProductStatus;
import com.example.order_service.order.application.port.dto.OrderProductsResult;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import com.example.order_service.order.exception.OrderProductPortErrorCode;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderProductPortMapper {

    public OrderProductsResult mapToOrderProductsResult(ProductResponse response) {
        if (response.products() == null || response.products().isEmpty()) {
            return OrderProductsResult.builder()
                    .products(Collections.emptyList())
                    .build();
        }
        List<OrderProductsResult.OrderProductDetail> products = response.products().stream().map(this::mapToOrderProduct).toList();
        return OrderProductsResult.builder()
                .products(products)
                .build();
    }

    private OrderProductsResult.OrderProductDetail mapToOrderProduct(ProductResponse.ProductDetail product) {
        ProductSnapshot productSnapshot = ProductSnapshot.of(product.productId(), product.productVariantId(),
                product.sku(), product.productName(), product.thumbnail());

        ProductResponse.UnitPrice unitPrice = product.unitPrice();
        ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(unitPrice.originalPrice()), unitPrice.discountRate(), Money.wons(unitPrice.discountAmount()),
                Money.wons(unitPrice.discountedPrice()));

        List<ProductOptionSnapshot> optionSnapshots = mapToProductOptionSnapshots(product.options());

        OrderProductStatus status = mapToProductStatus(product.status());

        return OrderProductsResult.OrderProductDetail.builder()
                .productSnapshot(productSnapshot)
                .status(status)
                .stock(product.stock())
                .priceSnapshot(priceSnapshot)
                .options(optionSnapshots)
                .build();
    }

    private List<ProductOptionSnapshot> mapToProductOptionSnapshots(List<ProductResponse.ProductOption> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }
        return options.stream().map(option -> ProductOptionSnapshot.of(option.optionTypeName(), option.optionValueName())).toList();
    }

    private OrderProductStatus mapToProductStatus(String productStatus) {
        return switch (productStatus) {
            case "ON_SALE" -> OrderProductStatus.ON_SALE;
            case "STOP_SALE" -> OrderProductStatus.STOP_SALE;
            case "DELETED" -> OrderProductStatus.DELETED;
            case "PREPARING" -> OrderProductStatus.PREPARING;
            case null, default -> throw new DefaultPortException(
                    OrderProductPortErrorCode.PRODUCT_CLIENT_ERROR,
                    "UNSUPPORTED_STATUS",
                    "처리할 수 없는 상품 상태입니다"
            );
        };
    }
}
