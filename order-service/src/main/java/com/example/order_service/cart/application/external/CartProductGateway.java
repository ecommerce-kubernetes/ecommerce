package com.example.order_service.cart.application.external;

import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.mapper.CartProductMapper;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.gateway.DefaultGatewayException;
import com.example.order_service.common.exception.gateway.ProductGatewayErrorCode;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.command.ProductCommand;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartProductGateway {
    private final ProductAdaptor productAdaptor;
    private final CartProductMapper mapper;

    public CartProductListResult getProducts(List<Long> variantIds) {
        ProductCommand.BulkSearch command = ProductCommand.BulkSearch.from(variantIds);
        ProductClientResponse.ProductList productList = executeGetProducts(command);
        return mapper.toResult(productList);
    }

    private ProductClientResponse.ProductList executeGetProducts(ProductCommand.BulkSearch command) {
        try {
            return productAdaptor.getProductsDeprecated(command);
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
