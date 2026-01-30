package com.example.order_service.api.cart.domain.service;

import com.example.order_service.api.cart.domain.model.ProductStatus;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo;
import com.example.order_service.api.cart.domain.service.dto.result.CartProductInfo.ProductOption;
import com.example.order_service.api.cart.infrastructure.client.CartProductAdaptor;
import com.example.order_service.api.cart.infrastructure.client.dto.CartProductResponse;
import com.example.order_service.api.common.exception.BusinessException;
import com.example.order_service.api.common.exception.CartErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartProductService {
    private final CartProductAdaptor cartProductAdaptor;

    public CartProductInfo getProductInfo(Long productVariantId) {
        CartProductResponse product = cartProductAdaptor.getProduct(productVariantId);
        validateProductOnSale(product);
        return mapToCartProductInfo(product);
    }

    private void validateProductOnSale(CartProductResponse product) {
        ProductStatus status = ProductStatus.from(product.getStatus());
        if (status != ProductStatus.ON_SALE) {
            throw new BusinessException(CartErrorCode.PRODUCT_NOT_ON_SALE);
        }
    }

    private CartProductInfo mapToCartProductInfo(CartProductResponse product) {
        List<ProductOption> options = product.getProductOptionInfos().stream().map(o -> ProductOption.of(o.getOptionTypeName(), o.getOptionValueName()))
                .toList();
        return CartProductInfo.builder()
                .productId(product.getProductId())
                .productVariantId((product.getProductVariantId()))
                .status(ProductStatus.from(product.getStatus()))
                .productName(product.getProductName())
                .originalPrice(product.getUnitPrice().getOriginalPrice())
                .discountRate(product.getUnitPrice().getDiscountRate())
                .discountAmount(product.getUnitPrice().getDiscountAmount())
                .discountedPrice(product.getUnitPrice().getDiscountedPrice())
                .productOption(options)
                .build();
    }
}
