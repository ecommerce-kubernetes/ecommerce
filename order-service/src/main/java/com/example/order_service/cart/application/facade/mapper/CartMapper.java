package com.example.order_service.cart.application.facade.mapper;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.domain.context.CreateCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CartMapper {

    @Mapping(target = "userId", source = "command.userId")
    @Mapping(target = "cartItemId", source = "cartItem.cartItemId")
    @Mapping(target = "quantity", source = "command.quantity")
    @Mapping(target = "maxLimit", source = "product.stock")
    UpdateCartItemContext toUpdateContext(
            UpdateCartItemQuantityCommand command,
            CartItemData cartItem,
            CartProductResult.CartProductDetail product
    );

    default CreateCartItemsContext toCreateContext(AddCartItemsCommand command, Map<Long, CartProductResult.CartProductDetail> productMap) {
        if (command == null) return null;

        List<CreateCartItemsContext.Item> items = command.items().stream()
                .map(item -> toCreateContextItem(item, productMap.get(item.productVariantId())))
                .toList();

        return CreateCartItemsContext.builder()
                .userId(command.userId())
                .items(items)
                .build();
    }

    @Mapping(target = "productVariantId", source = "item.productVariantId")
    @Mapping(target = "quantity", source = "item.quantity")
    @Mapping(target = "maxLimit", source = "product.stock")
    CreateCartItemsContext.Item toCreateContextItem(AddCartItemsCommand.Item item, CartProductResult.CartProductDetail product);
}
