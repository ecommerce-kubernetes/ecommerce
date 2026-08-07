package com.example.order_service.cart.application.service.mapper;

import com.example.order_service.cart.application.dto.command.AddCartItemsCommand;
import com.example.order_service.cart.application.dto.command.UpdateCartItemQuantityCommand;
import com.example.order_service.cart.application.dto.data.CartItemData;
import com.example.order_service.cart.application.port.dto.CartProductResult;
import com.example.order_service.cart.domain.context.AddCartItemsContext;
import com.example.order_service.cart.domain.context.UpdateCartItemContext;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-07T02:09:01+0900",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public UpdateCartItemContext toUpdateContext(UpdateCartItemQuantityCommand command, CartItemData cartItem, CartProductResult.CartProductDetail product) {
        if ( command == null && cartItem == null && product == null ) {
            return null;
        }

        UpdateCartItemContext.UpdateCartItemContextBuilder updateCartItemContext = UpdateCartItemContext.builder();

        if ( command != null ) {
            updateCartItemContext.quantity( command.quantity() );
        }
        if ( cartItem != null ) {
            updateCartItemContext.cartItemId( cartItem.cartItemId() );
        }
        if ( product != null ) {
            updateCartItemContext.maxLimit( product.stock() );
        }

        return updateCartItemContext.build();
    }

    @Override
    public AddCartItemsContext.Item toCreateContextItem(AddCartItemsCommand.Item item, CartProductResult.CartProductDetail product) {
        if ( item == null && product == null ) {
            return null;
        }

        AddCartItemsContext.Item.ItemBuilder item1 = AddCartItemsContext.Item.builder();

        if ( item != null ) {
            item1.productVariantId( item.productVariantId() );
            item1.quantity( item.quantity() );
        }
        if ( product != null ) {
            item1.maxLimit( product.stock() );
        }

        return item1.build();
    }
}
