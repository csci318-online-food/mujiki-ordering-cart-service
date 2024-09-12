package com.csci318.microservice.cart.Mappers;

import com.csci318.microservice.cart.DTOs.CartItemDTORequest;
import com.csci318.microservice.cart.DTOs.CartItemDTOResponse;
import com.csci318.microservice.cart.Entities.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartItemMapper implements Mapper<CartItem, CartItemDTOResponse, CartItemDTORequest> {

    @Override
    public CartItemDTOResponse toDtos(CartItem entity) {
        return new CartItemDTOResponse(
                entity.getId(),
                entity.getCartId(),
                entity.getOrderId(),
                entity.getRestaurantId(),
                entity.getItemId(),
                entity.getQuantity(),
                entity.getPrice()
        );
    }

    @Override
    public CartItem toEntities(CartItemDTORequest dto) {
        CartItem cartItem = new CartItem();
        cartItem.setOrderId(dto.getOrderId());
        cartItem.setRestaurantId(dto.getRestaurantId());
        cartItem.setItemId(dto.getItemId());
        cartItem.setPrice(dto.getPrice());
        cartItem.setCreateBy("system");
        cartItem.setModifyBy("system");
        return cartItem;
    }

    @Override
    public List<CartItemDTOResponse> toDtos(List<CartItem> entities) {
        return entities.stream().map(this::toDtos).collect(Collectors.toList());
    }

    @Override
    public List<CartItem> toEntities(List<CartItemDTORequest> dtos) {
        return dtos.stream().map(this::toEntities).collect(Collectors.toList());
    }

}

