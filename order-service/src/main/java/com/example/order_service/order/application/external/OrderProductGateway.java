package com.example.order_service.order.application.external;

import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.gateway.ProductGatewayErrorCode;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.order.application.external.dto.result.OrderProductResult;
import com.example.order_service.order.application.external.dto.result.OrderProductStatus;
import com.example.order_service.order.domain.vo.ProductOptionSnapshot;
import com.example.order_service.order.domain.vo.ProductPriceSnapshot;
import com.example.order_service.order.domain.vo.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderProductGateway {
    private final ProductAdaptor productAdaptor;

    public OrderProductResult getProducts(List<Long> productVariantIds) {
        ProductResponse response = executeGetProducts(productVariantIds);
        return mapToOrderProductResult(response);
    }

    private OrderProductResult mapToOrderProductResult(ProductResponse response) {
        List<OrderProductResult.OrderProductDetail> orderProductDetails = mapToOrderProductDetail(response.products());
        return OrderProductResult.builder()
                .products(orderProductDetails)
                .build();
    }

    private List<OrderProductResult.OrderProductDetail> mapToOrderProductDetail(List<ProductResponse.ProductDetail> products) {
        return products.stream().map(product -> {
            ProductSnapshot productSnapshot = ProductSnapshot.of(product.productId(), product.productVariantId(), product.sku(), product.productName(), product.thumbnail());
            ProductPriceSnapshot priceSnapshot = ProductPriceSnapshot.of(Money.wons(product.unitPrice().originalPrice()),
                    product.unitPrice().discountRate(), Money.wons(product.unitPrice().discountAmount()), Money.wons(product.unitPrice().discountedPrice()));
            return OrderProductResult.OrderProductDetail.builder()
                    .productSnapshot(productSnapshot)
                    .status(mapToProductStatus(product.status()))
                    .stock(product.stock())
                    .priceSnapshot(priceSnapshot)
                    .options(mapToOptions(product.options()))
                    .build();
        }).toList();
    }

    private OrderProductStatus mapToProductStatus(String productStatus) {
        return switch (productStatus) {
            case "ON_SALE" -> OrderProductStatus.ON_SALE;
            case "STOP_SALE" -> OrderProductStatus.STOP_SALE;
            case "DELETED" -> OrderProductStatus.DELETED;
            case "PREPARING" -> OrderProductStatus.PREPARING;
            case null, default -> throw new DefaultGatewayException(
                    ProductGatewayErrorCode.PRODUCT_CLIENT_ERROR,
                    "UNSUPPORTED_STATUS",
                    "처리할 수 없는 상품 상태입니다"
            );
        };
    }

    private List<ProductOptionSnapshot> mapToOptions(List<ProductResponse.ProductOption> options) {
        return options.stream().map(option -> ProductOptionSnapshot.of(option.optionTypeName(), option.optionValueName())).toList();
    }

    private ProductResponse executeGetProducts(List<Long> productVariantIds) {
        try {
            return productAdaptor.getProducts(productVariantIds);
        } catch (ExternalClientException e) {
            throw new DefaultGatewayException(ProductGatewayErrorCode.PRODUCT_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultGatewayException(ProductGatewayErrorCode.PRODUCT_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultGatewayException(ProductGatewayErrorCode.PRODUCT_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultGatewayException(ProductGatewayErrorCode.PRODUCT_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }
}
