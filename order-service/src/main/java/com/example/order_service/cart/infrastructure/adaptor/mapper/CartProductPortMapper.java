package com.example.order_service.cart.infrastructure.adaptor.mapper;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.common.domain.vo.Money;
import com.example.order_service.infrastructure.dto.response.product.ProductResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartProductPortMapper {

    public CartProductResult toCartProductResult(ProductResponse response) {
        List<CartProductResult.CartProductDetail> products = response.products().stream().map(this::toCartProductDetail).toList();
        return CartProductResult.builder()
                .products(products)
                .build();
    }

    private CartProductResult.CartProductDetail toCartProductDetail(ProductResponse.ProductDetail product) {
        List<CartProductResult.ProductOption> options = product.options().stream().map(this::toCartProductOption).toList();
        CartProductStatus status = toCartProductStatus(product.status());
        return CartProductResult.CartProductDetail.builder()
                .productId(product.productId())
                .productVariantId(product.productVariantId())
                .status(status)
                .stock(product.stock())
                .sku(product.sku())
                .productName(product.productName())
                .thumbnail(product.thumbnail())
                .originalPrice(Money.wons(product.unitPrice().originalPrice()))
                .discountRate(product.unitPrice().discountRate())
                .discountAmount(Money.wons(product.unitPrice().discountAmount()))
                .discountedPrice(Money.wons(product.unitPrice().discountedPrice()))
                .options(options)
                .build();
    }

    private CartProductResult.ProductOption toCartProductOption(ProductResponse.ProductOption option) {
        return CartProductResult.ProductOption.builder()
                .optionTypeName(option.optionTypeName())
                .optionValueName(option.optionValueName())
                .build();
    }

    private CartProductStatus toCartProductStatus(String status) {
        return switch (status) {
            case "PREPARING" -> CartProductStatus.PREPARING;
            case "ON_SALE" -> CartProductStatus.ON_SALE;
            case "STOP_SALE" -> CartProductStatus.STOP_SALE;
            case "DELETED" -> CartProductStatus.DELETED;
            default -> CartProductStatus.UNKNOWN;
        };
    }
}
