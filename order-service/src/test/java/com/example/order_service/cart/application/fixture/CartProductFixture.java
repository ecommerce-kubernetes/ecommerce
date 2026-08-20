package com.example.order_service.cart.application.fixture;

import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.application.port.dto.CartProductStatus;
import com.example.order_service.common.domain.vo.Money;

import java.util.List;

public class CartProductFixture {

    public static CartProductResult.CartProductResultBuilder anProducts() {
        return CartProductResult.builder()
                .products(List.of(anProduct().build()));
    }

    public static CartProductResult.CartProductDetail.CartProductDetailBuilder anProduct() {
        return CartProductResult.CartProductDetail.builder()
                .productId(1L)
                .productVariantId(1L)
                .status(CartProductStatus.ON_SALE)
                .stock(100)
                .sku("SKU")
                .productName("상품1")
                .thumbnail("/product/product.jpg")
                .originalPrice(Money.wons(10000L))
                .discountRate(10)
                .discountAmount(Money.wons(1000L))
                .discountedPrice(Money.wons(9000L))
                .options(List.of(anProductOption().build()));
    }

    public static CartProductResult.ProductOption.ProductOptionBuilder anProductOption() {
        return CartProductResult.ProductOption.builder()
                .optionTypeName("사이즈")
                .optionValueName("XL");
    }
}
