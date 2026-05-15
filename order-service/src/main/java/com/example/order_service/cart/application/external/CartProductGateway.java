package com.example.order_service.cart.application.external;

import com.example.order_service.cart.application.dto.result.CartProductResult;
import com.example.order_service.cart.application.mapper.CartProductMapper;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.business.BusinessException;
import com.example.order_service.common.exception.external.ExternalClientException;
import com.example.order_service.common.exception.external.ExternalServerException;
import com.example.order_service.common.exception.external.ExternalSystemUnavailableException;
import com.example.order_service.infrastructure.adaptor.ProductAdaptor;
import com.example.order_service.infrastructure.dto.response.ProductClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartProductGateway {
    private final ProductAdaptor adaptor;
    private final CartProductMapper mapper;

    public List<CartProductResult.Info> getProducts(List<Long> variantIds) {
        List<ProductClientResponse.ProductDeprecated> productDeprecateds = fetchProductWithTranslation(variantIds);
        return productDeprecateds.stream()
                .map(mapper::toResult)
                .toList();
    }

    // fallback
    private List<ProductClientResponse.ProductDeprecated> fetchProductWithTranslation(List<Long> ids) {
        try {
            return adaptor.getProductsByVariantIds(ids);
        } catch (ExternalClientException e) {
            // Client 에러 변환
            throw new BusinessException(CartErrorCode.CART_PRODUCT_CLIENT_ERROR);
        } catch (ExternalServerException e) {
            // Server 에러 변환
            throw new BusinessException(CartErrorCode.CART_PRODUCT_SERVER_ERROR);
        } catch (ExternalSystemUnavailableException e) {
            // 서킷브레이커 요청 블로킹, 503 에러
            throw new BusinessException(CartErrorCode.CART_PRODUCT_UNAVAILABLE_SERVER_ERROR);
        }
    }
}
