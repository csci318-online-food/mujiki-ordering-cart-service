package com.csci318.microservice.cart.Services;

import com.csci318.microservice.cart.DTOs.CartDTORequest;
import com.csci318.microservice.cart.DTOs.CartDTOResponse;
import com.csci318.microservice.cart.DTOs.CartItemDTORequest;

import java.util.UUID;

public interface CartService {
    CartDTOResponse createCart(CartDTORequest cartDTORequest);
    CartDTOResponse addItemToCart(UUID cartId, CartItemDTORequest cartItemRequest);
}
