package com.example.order_service.cart.infrastructure.adaptor.client;

import com.example.order_service.cart.application.port.CartProductPort;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.infrastructure.adaptor.mapper.CartProductPortMapper;
import com.example.order_service.common.exception.external.ExternalCircuitBreakerException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.common.exception.port.DefaultPortException;
import com.example.order_service.common.exception.port.ProductPortErrorCode;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import com.example.order_service.infrastructure.gateway.ProductGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartProductAdaptor implements CartProductPort {
    private final ProductGateway productGateway;
    private final CartProductPortMapper mapper;

    @Override
    public CartProductResult getProducts(List<Long> productVariantIds) {
        ProductResponse response = executeGetProducts(productVariantIds);
        return mapper.toCartProductResult(response);
    }

    private ProductResponse executeGetProducts(List<Long> productVariantIds) {
        try {
            return productGateway.getProducts(productVariantIds);
        } catch (ExternalClientException e) {
            throw new DefaultPortException(ProductPortErrorCode.PRODUCT_CLIENT_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalServerException e) {
            throw new DefaultPortException(ProductPortErrorCode.PRODUCT_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalSystemUnavailableException e) {
            throw new DefaultPortException(ProductPortErrorCode.PRODUCT_UNAVAILABLE_SERVER_ERROR, e.getErrorCode(), e.getMessage());
        } catch (ExternalCircuitBreakerException e) {
            throw new DefaultPortException(ProductPortErrorCode.PRODUCT_CIRCUIT_OPEN, e.getErrorCode(), e.getMessage());
        }
    }
}
