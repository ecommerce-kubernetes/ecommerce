package com.example.order_service.cart.application.external;

import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.gateway.ProductGatewayErrorCode;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartProductGateway {
    private final ProductAdaptor productAdaptor;

    public CartProductResult getProducts(List<Long> variantIds) {
        ProductResponse response = executeGetProducts(variantIds);
        return mapToCartProductResult(response);
    }

    private CartProductResult mapToCartProductResult(ProductResponse response) {
        List<CartProductResult.CartProductDetail> products = mapToCartProductDetail(response.products());
        return CartProductResult.builder()
                .products(products)
                .build();
    }

    private List<CartProductResult.CartProductDetail> mapToCartProductDetail(List<ProductResponse.ProductDetail> products) {
        return products.stream().map(product -> CartProductResult.CartProductDetail.builder()
                .productId(product.productId())
                .productVariantId(product.productVariantId())
                .status(mapToProductStatus(product.status()))
                .stock(product.stock())
                .sku(product.sku())
                .productName(product.productName())
                .thumbnail(product.thumbnail())
                .originalPrice(Money.wons(product.unitPrice().originalPrice()))
                .discountRate(product.unitPrice().discountRate())
                .discountAmount(Money.wons(product.unitPrice().discountAmount()))
                .discountedPrice(Money.wons(product.unitPrice().discountedPrice()))
                .options(mapToOptions(product.options()))
                .build()).toList();
    };

    private List<CartProductResult.ProductOption> mapToOptions(List<ProductResponse.ProductOption> options) {
        return options.stream().map(option -> CartProductResult.ProductOption.builder()
                .optionTypeName(option.optionTypeName()).optionValueName(option.optionValueName()).build()).toList();
    }

    private CartProductStatus mapToProductStatus(String productStatus) {
        return switch (productStatus) {
            case "ON_SALE" -> CartProductStatus.ON_SALE;
            case "STOP_SALE" -> CartProductStatus.STOP_SALE;
            case "DELETED" -> CartProductStatus.DELETED;
            case "PREPARING" -> CartProductStatus.PREPARING;
            case null, default -> throw new DefaultGatewayException(
                    ProductGatewayErrorCode.PRODUCT_CLIENT_ERROR,
                    "UNSUPPORTED_STATUS",
                    "처리할 수 없는 상품 상태입니다"
            );
        };
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
