package com.csci318.microservice.cart.Mappers;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTOResponse;
import com.csci318.microservice.cart.Domain.Entities.Cart;
import com.csci318.microservice.cart.Domain.Entities.CartItem;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper implements Mapper<Cart, CartDTOResponse, CartDTORequest> {

    @Override
    public CartDTOResponse toDtos(Cart entity) {
        return new CartDTOResponse(
                entity.getId(),
                entity.getUserId(),
                entity.getRestaurantId(),
                entity.getTotalPrice()
        );
    }

    @Override
    public Cart toEntities(CartDTORequest dto) {
        Cart cart = new Cart();
        cart.setUserId(dto.getUserId());
//        cart.setRestaurantId(dto.getRestaurantId());
        cart.setTotalPrice(0.0); // Initialize total price
        return cart;
    }

    @Override
    public List<CartDTOResponse> toDtos(List<Cart> entities) {
        return entities.stream().map(this::toDtos).collect(Collectors.toList());
    }

    @Override
    public List<Cart> toEntities(List<CartDTORequest> dtos) {
        return dtos.stream().map(this::toEntities).collect(Collectors.toList());
    }

    // Helper method to convert CartItem entity to CartItemResponse
    private CartItemDTOResponse convertCartItemToDto(CartItem cartItem) {
        return new CartItemDTOResponse(
                cartItem.getId(),
                cartItem.getCartId(),
                cartItem.getRestaurantId(),
                cartItem.getItemId(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }

}

