package com.example.order_service.cart.application.facade;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;

import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.dto.result.CartItemAvailability;
import com.example.order_service.cart.application.external.CartProductGateway;
import com.example.order_service.cart.application.external.dto.CartProductListResult;
import com.example.order_service.cart.application.external.dto.CartProductResult;
import com.example.order_service.cart.application.external.dto.CartProductStatus;
import com.example.order_service.cart.application.service.CartCommandService;
import com.example.order_service.cart.application.service.CartQueryService;
import com.example.order_service.cart.application.dto.result.CartResult;
import com.example.order_service.cart.exception.CartErrorCode;
import com.example.order_service.common.exception.application.BusinessException;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartFacadeTest {

    @InjectMocks
    private CartFacade cartFacade;
    @Mock
    private CartProductGateway cartProductGateway;
    @Mock
    private CartCommandService cartCommandService;
    @Mock
    private CartQueryService cartQueryService;
    @Mock
    private CartItemValidator validator;

    @Nested
    @DisplayName("장바구니 추가")
    class AddItems {
    }

    private AddCartItemsCommand createAddCommand(Long productVariantId, int quantity) {
        AddCartItemsCommand.Item item = Instancio.of(AddCartItemsCommand.Item.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("quantity"), quantity)
                .create();

        return Instancio.of(AddCartItemsCommand.class)
                .set(field("items"), List.of(item))
                .create();
    }

    private CartProductListResult createProductList(Long productVariantId, CartProductStatus status) {
        CartProductResult product = Instancio.of(CartProductResult.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("status"), status)
                .create();
        return Instancio.of(CartProductListResult.class)
                .set(field("products"), List.of(product))
                .create();
    }

    private CartItemData createCartItemData(Long productVariantId, int quantity) {
        return Instancio.of(CartItemData.class)
                .set(field("productVariantId"), productVariantId)
                .set(field("quantity"), quantity)
                .create();
    }
}
